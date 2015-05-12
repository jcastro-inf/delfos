package delfos.io.database.mysql.dataset;

import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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

    private final String varcharSize = "200";
    private final String intSize = "11";

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
                beginingOfInsert.append(",`").append(itemFeature.getName()).append("`");

            }
            beginingOfInsert.append(") VALUES (");

        }

        for (Item item : contentDataset) {
            int idItem = item.getId();
            StringBuilder insert = new StringBuilder();
            insert.append(beginingOfInsert);

            //Escribir el id y el nombre
            insert.append(idItem);
            insert.append(",'");
            insert.append(item.getName().replaceAll("'", "''"));
            insert.append("'");

            for (Feature itemFeature : features) {
                insert.append(",");
                switch (itemFeature.getType()) {
                    case Nominal:
                        insert.append("'");
                        insert.append(item.getFeatureValue(itemFeature).toString().replaceAll("'", "''"));
                        insert.append("'");
                        break;
                    case Numerical:
                        insert.append(item.getFeatureValue(itemFeature).toString());
                        break;
                    default:
                        insert.append("'");
                        insert.append(item.getFeatureValue(itemFeature).toString().replaceAll("'", "''"));
                        insert.append("'");
                        break;
                }
            }
            insert.append(");");
            mySQLConnection.execute(insert.toString());
        }
        mySQLConnection.commit();
    }

    public ContentDataset readDataset() throws SQLException {

        FeatureGenerator featureGenerator = new FeatureGenerator();
        List<Item> items = new LinkedList<>();

        String consulta_getFeatures = "SELECT " + featureTable_featureName + "," + featureTable_featureType + " "
                + "FROM " + featureTableName + ";";

        //Leo las características.
        try (ResultSet rstFeatures = mySQLConnection.executeQuery(consulta_getFeatures)) {
            while (rstFeatures.next()) {
                String featureName = rstFeatures.getString(1);
                String type = rstFeatures.getString(2);
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
            consulta_getItems.append(",`").append(f.getName()).append("`");
        }

        consulta_getItems.append(" FROM ").append(itemsTableName).append(";");

        ResultSet rstItems = mySQLConnection.executeQuery(consulta_getItems.toString());

        while (rstItems.next()) {
            int idItem = rstItems.getInt(itemsTable_idItem);
            String name = rstItems.getString(itemsTable_itemName);

            int index = 0;

            Object[] values = new Object[featureGenerator.getSortedFeatures().size()];
            for (Feature itemFeature : featureGenerator.getSortedFeatures()) {
                switch (itemFeature.getType()) {
                    case Nominal:
                        values[index] = rstItems.getString(itemFeature.getName());
                        break;
                    case Numerical:
                        values[index] = rstItems.getDouble(itemFeature.getName());
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

        try {
            return new ContentDatasetDefault(items);
        } catch (ItemAlreadyExists ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void createTables(ContentDataset contentDataset) throws SQLException {

        deleteTables();

        Feature[] itemFeatures = contentDataset.getFeatures();

        String createFeaturesTable = "CREATE TABLE " + featureTableName + "("
                + featureTable_featureName + " VARCHAR(" + varcharSize + ") NOT NULL,"
                + featureTable_featureType + " VARCHAR(" + varcharSize + ") NOT NULL,"
                + "PRIMARY KEY(" + featureTable_featureName + "));";

        mySQLConnection.execute(createFeaturesTable);

        for (Feature itemFeature : itemFeatures) {
            final String insertFeature = "INSERT INTO " + featureTableName + " "
                    + "(" + featureTable_featureName
                    + "," + featureTable_featureType + ") "
                    + "VALUES ('" + itemFeature.getName().replaceAll("'", "''") + "','" + itemFeature.getType().name() + "');";
            mySQLConnection.execute(insertFeature);
        }

        StringBuilder createContentTable = new StringBuilder();

        createContentTable.append("CREATE TABLE `");
        createContentTable.append(itemsTableName);
        createContentTable.append("` (\n");
        createContentTable.append("\t").append("`").append(itemsTable_idItem).append("` INT(" + intSize + ") NOT NULL,\n");
        createContentTable.append("\t").append("`").append(itemsTable_itemName).append("` VARCHAR(" + varcharSize + ") DEFAULT NULL,\n");

        for (Feature itemFeature : itemFeatures) {

            createContentTable.append("\t").append("`").append(itemFeature.getName()).append("`");
            switch (itemFeature.getType()) {
                case Nominal:
                    createContentTable.append(" VARCHAR(" + varcharSize + ") DEFAULT NULL,");
                    break;
                case Numerical:
                    createContentTable.append(" FLOAT DEFAULT NULL,");
                    break;
                default:
                    throw new IllegalArgumentException("Not implemented for '" + itemFeature.getType() + "'");
            }

            createContentTable.append("\n");
        }

        createContentTable.append("PRIMARY KEY (");
        createContentTable.append(itemsTable_idItem);
        createContentTable.append(") );");

        mySQLConnection.execute(createContentTable.toString());

    }

    private void deleteTables() throws SQLException {
        mySQLConnection.execute("DROP TABLE IF EXISTS " + itemsTableName + ";");
        mySQLConnection.execute("DROP TABLE IF EXISTS " + featureTableName + ";");
    }

}
