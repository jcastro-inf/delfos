package delfos.io.database.mysql.dataset;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;

/**
 * Clase para escribir un dataset de contenido a una base de datos MySQL.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 11-Mar-2013
 */
public class ContentDatasetToMySQL {

    public static final String CORE_FEATURES_TABLE_NAME = "item_features";
    public static final String CORE_FEATURES_COLUMN_NAME = "name";
    public static final String CORE_FEATURES_COLUMN_TYPE = "type";
    public static final String CORE_ITEMS_TABLE_NAME = "items";
    public static final String CORE_ITEMS_COLUMN_ID = "iditem";
    public static final String CORE_ITEMS_COLUMN_NAME = "name";

    private final String featureTableName;
    private final String featureTable_featureName, featureTable_featureType;

    private final String itemsTableName;
    private final String itemsTable_idItem, itemsTable_itemName;

    private final MySQLConnection mySQLConnection;

    public ContentDatasetToMySQL(MySQLConnection mySQLConnection) {
        this.mySQLConnection = mySQLConnection;

        featureTableName = mySQLConnection.getPrefix() + CORE_FEATURES_TABLE_NAME;

        featureTable_featureName = CORE_FEATURES_COLUMN_NAME;
        featureTable_featureType = CORE_FEATURES_COLUMN_TYPE;

        itemsTableName = mySQLConnection.getPrefix() + CORE_ITEMS_TABLE_NAME;
        itemsTable_idItem = CORE_ITEMS_COLUMN_ID;
        itemsTable_itemName = CORE_ITEMS_COLUMN_NAME;
    }

    public void writeDataset(ContentDataset contentDataset) throws SQLException {

        createTables(contentDataset);
        Feature[] features = contentDataset.getFeatures();
        StringBuilder beginingOfInsert = new StringBuilder();
        {
            beginingOfInsert.append("INSERT INTO ");
            beginingOfInsert.append(itemsTableName);
            beginingOfInsert.append(" (");
            beginingOfInsert.append(itemsTable_idItem);
            beginingOfInsert.append(",");
            beginingOfInsert.append(itemsTable_itemName);
            for (Feature itemFeature : features) {
                beginingOfInsert.append(",").append(itemFeature.getName());

            }
            beginingOfInsert.append(") VALUES (");

        }
        try (Connection connection = mySQLConnection.doConnection();
                Statement statement = connection.createStatement()) {

            for (Item item : contentDataset) {
                int idItem = item.getId();
                StringBuilder insert = new StringBuilder();
                insert.append(beginingOfInsert);

                //Escribir el id y el nombre
                insert.append(idItem);
                insert.append(",'");
                insert.append(item.getName());
                insert.append("'");

                for (Feature itemFeature : features) {
                    insert.append(",");
                    switch (itemFeature.getType()) {
                        case Nominal:
                            insert.append("'");
                            insert.append(item.getFeatureValue(itemFeature).toString());
                            insert.append("'");
                            break;
                        case Numerical:
                            insert.append(item.getFeatureValue(itemFeature).toString());
                            break;
                        default:
                            insert.append("'");
                            insert.append(item.getFeatureValue(itemFeature).toString());
                            insert.append("'");
                            break;
                    }
                }
                insert.append(");");

                Global.showMessage("===================\n");
                Global.showMessage(insert + "\n");
                Global.showMessage("===================\n");
                statement.executeUpdate(insert.toString());
            }
        }
    }

    public ContentDataset readDataset() throws SQLException {

        FeatureGenerator featureGenerator = new FeatureGenerator();
        List<Item> items = new LinkedList<>();

        String consulta_getFeatures = "SELECT " + featureTable_featureName + "," + featureTable_featureType + " "
                + "FROM " + featureTableName + ";";

        //Leo las características.
        try (Connection connection = mySQLConnection.doConnection();
                Statement statement = connection.createStatement();
                ResultSet rst = statement.executeQuery(consulta_getFeatures)) {

            while (rst.next()) {
                String featureName = rst.getString(1);
                String type = rst.getString(2);
                FeatureType featureType = FeatureType.getFeatureType(type);

                if (!featureGenerator.containsFeature(featureName)) {
                    featureGenerator.createFeature(featureName, featureType);
                }
            }
        }

        //Consulta para leer los productos
        StringBuilder consulta_getItems = new StringBuilder();
        consulta_getItems.append("SELECT ").append(itemsTable_idItem).append(", ").append(itemsTable_itemName);

        for (Feature f : featureGenerator.getSortedFeatures()) {
            consulta_getItems.append(", ");
            consulta_getItems.append(f.getName());
        }

        consulta_getItems.append(" FROM ").append(itemsTableName).append(";");

        try (Connection connection = mySQLConnection.doConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet rst = statement.executeQuery(consulta_getItems.toString())) {

                    while (rst.next()) {
                        int idItem = rst.getInt(itemsTable_idItem);
                        String name = rst.getString(itemsTable_itemName);

                        int index = 0;

                        Object[] values = new Object[featureGenerator.getSortedFeatures().size()];
                        for (Feature itemFeature : featureGenerator.getSortedFeatures()) {
                            switch (itemFeature.getType()) {
                                case Nominal:
                                    values[index] = rst.getString(itemFeature.getName());
                                    break;
                                case Numerical:
                                    values[index] = rst.getDouble(itemFeature.getName());
                                    break;
                                default:
                                    throw new UnsupportedOperationException("Item attribute type '" + itemFeature.getType() + "' not supported yet.");
                            }
                            index++;
                        }
                        items.add(new Item(
                                idItem,
                                name,
                                featureGenerator.getSortedFeatures().toArray(new Feature[0]),
                                values));
                    }
                } catch (SQLException ex) {
                    Global.showWarning("error fetching results of query: " + consulta_getItems);
                    throw ex;
                }
            } catch (SQLException ex) {
                Global.showWarning("error in query: " + consulta_getItems);
                throw ex;
            }
        }
        try {
            return new ContentDatasetDefault(items);
        } catch (ItemAlreadyExists ex) {
            Global.showWarning("QUERY FAILED");
            throw new IllegalStateException(ex);
        }
    }

    private void createTables(ContentDataset contentDataset) throws SQLException {

        deleteTables();

        Feature[] itemFeatures = contentDataset.getFeatures();

        String createFeaturesTable = "CREATE TABLE " + featureTableName + " ("
                + featureTable_featureName + " VARCHAR(45) NOT NULL,"
                + featureTable_featureType + " VARCHAR(45) NOT NULL,"
                + "PRIMARY KEY(" + featureTable_featureName + "));";

        try (Connection connection = mySQLConnection.doConnection();
                Statement statement = connection.createStatement()) {

            statement.execute(createFeaturesTable);

            for (Feature itemFeature : itemFeatures) {
                statement.executeUpdate("INSERT INTO " + featureTableName + " "
                        + "(" + featureTable_featureName + "," + featureTable_featureType + ") "
                        + "VALUES ('" + itemFeature.getName() + "','" + itemFeature.getType().name() + "');");
            }

            StringBuilder createContentTable = new StringBuilder();

            createContentTable.append("CREATE TABLE ");
            createContentTable.append(itemsTableName);
            createContentTable.append(" (");
            createContentTable.append(itemsTable_idItem);
            createContentTable.append(" INT NOT NULL ,");
            createContentTable.append(itemsTable_itemName);
            createContentTable.append(" VARCHAR(45) NULL ,");

            for (Feature itemFeature : itemFeatures) {

                createContentTable.append(itemFeature.getName());
                switch (itemFeature.getType()) {
                    case Nominal:
                        createContentTable.append(" VARCHAR(45) NULL ,");
                        break;
                    case Numerical:
                        createContentTable.append(" FLOAT NULL ,");
                        break;
                    default:
                        throw new IllegalArgumentException("Not implemented for '" + itemFeature.getType() + "'");
                }
            }

            createContentTable.append("PRIMARY KEY (");
            createContentTable.append(itemsTable_idItem);
            createContentTable.append(") );");

            statement.execute(createContentTable.toString());
        }
    }

    private void deleteTables() throws SQLException {
        try (Connection connection = mySQLConnection.doConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + itemsTableName + ";");
            statement.execute("DROP TABLE IF EXISTS " + featureTableName + ";");
        }
    }
}
