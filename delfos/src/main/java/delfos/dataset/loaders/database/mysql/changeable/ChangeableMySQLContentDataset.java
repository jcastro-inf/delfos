package delfos.dataset.loaders.database.mysql.changeable;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.LockedIterator;
import delfos.common.exceptions.dataset.entity.EntityAlreadyExists;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeatures;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.changeable.ChangeableContentDataset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Implementa un dataset de contenido con persistencia sobre base de datos MySQL
 * con la posibilidad de modificar los productos del mismo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 04-Diciembre-2013
 */
public class ChangeableMySQLContentDataset implements ChangeableContentDataset, CollectionOfEntitiesWithFeatures<Item> {

    private ContentDatasetDefault contentDataset;
    private final FeatureGenerator featureGenerator = new FeatureGenerator();
    private final MySQLConnection mySQLConnection;
    private final String contentDefinitionTable_name;
    private final String contentDefinitionTable_FeatureIdField;
    private final String contentDefinitionTable_FeatureNameField;
    private final String contentDefinitionTable_FeatureTypeField;
    private final String productsTable_name;
    private final String productsTable_ItemIDField;
    private final String productsTable_NameField;
    private final String productsTable_AvailabilityField;

    ChangeableMySQLContentDataset(
            MySQLConnection connection,
            String contentDefinitionTable_name,
            String contentDefinitionTable_FeatureIdField,
            String contentDefinitionTable_FeatureNameField,
            String contentDefinitionTable_FeatureTypeField,
            String productsTable_name,
            String productsTable_ItemIDField,
            String productsTable_NameField,
            String productsTable_AvailabilityField) {

        this.mySQLConnection = connection;
        this.contentDefinitionTable_name = contentDefinitionTable_name;
        this.contentDefinitionTable_FeatureIdField = contentDefinitionTable_FeatureIdField;
        this.contentDefinitionTable_FeatureNameField = contentDefinitionTable_FeatureNameField;
        this.contentDefinitionTable_FeatureTypeField = contentDefinitionTable_FeatureTypeField;
        this.productsTable_name = productsTable_name;
        this.productsTable_ItemIDField = productsTable_ItemIDField;
        this.productsTable_NameField = productsTable_NameField;
        this.productsTable_AvailabilityField = productsTable_AvailabilityField;

        boolean allOK = true;

        if (!MySQLConnection.existsTable(connection, contentDefinitionTable_name)) {

            Global.showWarning("Content definition table '" + contentDefinitionTable_name + "' in database '" + mySQLConnection.getDatabaseName() + "' not exists");

            allOK = false;
        }

        if (!MySQLConnection.existsTable(connection, productsTable_name)) {
            Global.showWarning("Products table '" + productsTable_name + "' in database '" + mySQLConnection.getDatabaseName() + "' not exists");
            allOK = false;
        }

        if (allOK) {
            try {
                readContentDataset();
            } catch (SQLException ex) {
                ERROR_CODES.DATABASE_NOT_READY.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        } else {
            Global.showWarning(ChangeableMySQLContentDataset.class + ": Empty content dataset.");
            contentDataset = new ContentDatasetDefault();
        }

    }

    @Override
    public final void addItem(Item item) throws ItemAlreadyExists {
        try {
            if (contentDataset.getAllID().contains(item.getId())) {
                //El dataset ya tenía el producto, haciento cambio.

                Map<Integer, Item> items = new TreeMap<>();

                for (Item item2 : contentDataset) {
                    items.put(item2.getId(), item2);
                }

                items.remove(item.getId());
                items.put(item.getId(), item);

                try {
                    contentDataset = new ContentDatasetDefault(items.values());
                } catch (ItemAlreadyExists ex) {
                    //TODO: Este error nunca se produce
                    ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                }
            }

            boolean necesarioRegenerarTabla = false;

            for (Feature feature : item.getFeatures()) {
                if (featureGenerator.searchFeature(feature.getName()) == null) {
                    //El producto tiene nuevas características, se necesita añadir una columna.
                    necesarioRegenerarTabla = true;
                    break;
                }
            }

            try {
                if (necesarioRegenerarTabla) {
                    createItemsTable();
                } else {
                    insertItemInTable(item);
                }
            } catch (SQLException ex) {
                ERROR_CODES.DATABASE_NOT_READY.exit(ex);
            }

            // TODO hay algo por hacer...
        } catch (EntityAlreadyExists ex) {
            throw new ItemAlreadyExists(item.getId(), ex);
        }
    }

    @Override
    public Item get(int idItem) throws EntityNotFound {
        return contentDataset.get(idItem);
    }

    @Override
    public void commitChangesInPersistence() {
        // No need for commit changes, they are already in the database

        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            statement.execute("COMMIT;");
            statement.close();
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
        }
    }

    protected void createTables() throws SQLException {
        createItemFeaturesTable();
        createItemsTable();
    }

    public void createItemFeaturesTable() throws SQLException {
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            String dropTable = "drop table if exists " + getContentDefinitionTable_name() + ";";
            statement.execute(dropTable);
            String createTable = "CREATE TABLE IF NOT EXISTS `" + getContentDefinitionTable_name() + "` (\n"
                    + "`" + contentDefinitionTable_FeatureIdField + "` int(11) NOT NULL,\n"
                    + "`" + contentDefinitionTable_FeatureNameField + "` varchar(255) NOT NULL,\n"
                    + "`" + contentDefinitionTable_FeatureTypeField + "` varchar(255) NOT NULL,\n"
                    + "  PRIMARY KEY (`" + contentDefinitionTable_FeatureNameField + "`)\n"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;";
            statement.execute(createTable);
        }
    }

    protected void createItemsTable() throws SQLException {
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            Feature[] features = contentDataset.getFeatures();

            String clearFeaturesTable = "delete from " + getContentDefinitionTable_name() + ";";
            int clearFeaturesTableResult = statement.executeUpdate(clearFeaturesTable);
            if (clearFeaturesTableResult != 0) {
                Global.showInfoMessage("Features table '" + getContentDefinitionTable_name() + "' cleared due to new item features (" + clearFeaturesTableResult + " rows deleted).\n");
            }

            for (int i = 0; i < features.length; i++) {

                Feature feature = features[i];
                String insert = "insert into " + getContentDefinitionTable_name() + "(" + contentDefinitionTable_FeatureIdField + "," + contentDefinitionTable_FeatureNameField + "," + contentDefinitionTable_FeatureTypeField + ")"
                        + " values (" + i + ",'" + feature.getName() + "','" + feature.getType().name() + "');";
                statement.executeUpdate(insert);
            }

            String dropTable = "drop table if exists " + getProductsTable_name() + ";";
            statement.execute(dropTable);

            StringBuilder createItemsTable = new StringBuilder();
            createItemsTable.append("CREATE TABLE IF NOT EXISTS `").append(getProductsTable_name()).append("` (\n");
            createItemsTable.append("`").append(productsTable_ItemIDField).append("` int(11) NOT NULL,\n");
            createItemsTable.append("`").append(productsTable_AvailabilityField).append("` varchar(255) NOT NULL,\n");
            createItemsTable.append("`").append(productsTable_NameField).append("` varchar(255) NOT NULL,\n");

            for (Feature feature : features) {
                String fieldType = feature.getType().getMySQLfieldType();
                createItemsTable.append("`").append(feature.getName()).append("` ").append(fieldType).append(",\n");
            }
            createItemsTable.append("  PRIMARY KEY (`").append(productsTable_ItemIDField).append("`)\n");
            createItemsTable.append(") ENGINE=MyISAM DEFAULT CHARSET=latin1;");

            statement.execute(createItemsTable.toString());

        }
        for (Item item : contentDataset) {
            insertItemInTable(item);
        }

    }

    @Override
    public int size() {
        return contentDataset.size();
    }

    @Override
    public Collection<Integer> allID() {
        return contentDataset.allID();
    }

    @Override
    public Collection<Integer> getAvailableItems() {
        return contentDataset.getAvailableItems();
    }

    @Override
    public void setItemAvailable(int idItem, boolean available) throws ItemNotFound {
        contentDataset.setItemAvailable(idItem, available);
    }

    @Override
    public int compareTo(Object o) {
        return contentDataset.compareTo(o);
    }

    @Override
    public Feature[] getFeatures() {
        return contentDataset.getFeatures();
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        return contentDataset.getAllFeatureValues(feature);
    }

    @Override
    public double getMinValue(Feature feature) {
        return contentDataset.getMinValue(feature);
    }

    @Override
    public double getMaxValue(Feature feature) {
        return contentDataset.getMaxValue(feature);
    }

    @Override
    public Feature searchFeature(String featureName) {
        return contentDataset.searchFeature(featureName);
    }

    @Override
    public Feature searchFeatureByExtendedName(String extendedName) {
        return contentDataset.searchFeatureByExtendedName(extendedName);
    }

    @Override
    public Map<Feature, Object> parseEntityFeatures(Map<String, String> features) {
        return contentDataset.parseEntityFeatures(features);
    }

    @Override
    public Collection<Integer> getAllID() {
        return contentDataset.getAllID();
    }

    @Override
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(int idEntity, Map<String, String> features) throws EntityNotFound {
        return contentDataset.parseEntityFeaturesAndAddToExisting(idEntity, features);
    }

    @Override
    public Iterator<Item> iterator() {
        return new LockedIterator<Item>(contentDataset.iterator());
    }

    @Override
    public void add(Item entity) throws EntityAlreadyExists {
        try {
            addItem(entity);
        } catch (ItemAlreadyExists ex) {
            throw new EntityAlreadyExists(entity.getId(), ex);
        }
    }

    private void insertItemInTable(Item item) throws SQLException {

        try (Statement statement = mySQLConnection.doConnection().createStatement()) {

            //Borro el producto.
            String deleteProduct = "delete from " + getProductsTable_name() + " where " + productsTable_ItemIDField + " = " + item.getId() + ";";
            int deleteProductResult = statement.executeUpdate(deleteProduct);
            if (deleteProductResult != 0) {
                Global.showInfoMessage("Product " + item.getId() + " deleted from table " + getProductsTable_name() + "\n");
            }

            //Insert de cada item.
            StringBuilder insertItem = new StringBuilder();

            insertItem.append("insert into ").append(getProductsTable_name()).append(" (").append(productsTable_ItemIDField).append(",").append(productsTable_NameField).append(",").append(productsTable_AvailabilityField);

            for (Feature feature : item.getFeatures()) {
                insertItem.append(",`").append(feature.getName()).append("`");
            }

            insertItem.append(") values (").append(item.getId()).append(",'").append(item.getName()).append("',true");

            for (Feature feature : item.getFeatures()) {
                Object featureValue = item.getFeatureValue(feature);
                insertItem.append(",");
                if (feature.getType() == FeatureType.Numerical) {
                    insertItem.append(featureValue);
                } else {
                    insertItem.append("'").append(item.getFeatureValue(feature)).append("'");
                }
            }

            insertItem.append(");");
            int insertItemResult = statement.executeUpdate(insertItem.toString());
            if (insertItemResult != 0) {
                Global.showInfoMessage("Product " + item.getId() + " inserted into table " + getProductsTable_name() + "\n");
            }
        }
    }

    private void readContentDataset() throws SQLException {
        List<Item> items = new LinkedList<>();

        //Leo las características
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            String selectFeatures = "Select " + contentDefinitionTable_FeatureIdField + "," + contentDefinitionTable_FeatureNameField + "," + contentDefinitionTable_FeatureTypeField
                    + " from " + getContentDefinitionTable_name() + ";";

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Reading content dataset:\n");

                Global.showInfoMessage("\tReading features: " + selectFeatures + "\n");
            }

            ResultSet result = statement.executeQuery(selectFeatures);
            while (result.next()) {

                String featureName = result.getString(contentDefinitionTable_FeatureNameField);
                String featureType = result.getString(contentDefinitionTable_FeatureTypeField);

                if (!featureGenerator.containsFeature(featureName)) {
                    featureGenerator.createFeature(featureName, FeatureType.getFeatureType(featureType));
                }
            }

            StringBuilder selectItems = new StringBuilder();

            selectItems.append("Select ");

            selectItems.append(productsTable_ItemIDField);
            selectItems.append(",").append(productsTable_NameField);
            selectItems.append(",").append(productsTable_AvailabilityField);

            for (Feature feature : featureGenerator.getSortedFeatures()) {
                selectItems.append(",").append("`").append(feature.getName()).append("`");
            }

            selectItems.append(" From ").append(getProductsTable_name());

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("\tReading items: " + selectItems.toString() + "\n");
            }

            ResultSet selectItemResult = statement.executeQuery(selectItems.toString());

            Set<Integer> availables = new TreeSet<>();

            while (selectItemResult.next()) {
                int idItem = selectItemResult.getInt(productsTable_ItemIDField);
                String name = selectItemResult.getString(productsTable_NameField);
                boolean available = selectItemResult.getBoolean(productsTable_AvailabilityField);
                if (available) {
                    availables.add(idItem);
                }

                Map<Feature, Object> itemFeatures = new TreeMap<>();

                for (Feature feature : featureGenerator.getSortedFeatures()) {
                    final String column = feature.getName();

                    String featureValueString = selectItemResult.getString(column);
                    Object featureValue = feature.getType().parseFeatureValue(featureValueString);

                    if (!selectItemResult.wasNull()) {
                        itemFeatures.put(feature, featureValue);
                    }
                }
                Item item = new Item(idItem, name, itemFeatures);

                items.add(item);

            }

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Items readed : " + items.size() + "\n");
            }
            try {
                contentDataset = new ContentDatasetDefault(items);
            } catch (ItemAlreadyExists ex) {
                ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        }
    }

    public String getContentDefinitionTable_name() {
        return mySQLConnection.getPrefix() + contentDefinitionTable_name;
    }

    public String getProductsTable_name() {
        return mySQLConnection.getPrefix() + productsTable_name;
    }

    @Override
    public String toString() {
        Set<String> items = new TreeSet<>();
        for (Item item : contentDataset) {
            items.add("Item " + item.getId());
        }
        return items.toString();
    }

    @Override
    public Item getItem(int idItem) throws ItemNotFound {
        try {
            return get(idItem);
        } catch (EntityNotFound ex) {
            ex.isA(ItemNotFound.class);
            throw new ItemNotFound(idItem, ex);
        }
    }
}
