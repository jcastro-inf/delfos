package delfos.io.database.mysql.dataset;

import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeSet;

/**
 * Clase para escribir un dataset de contenido a una base de datos MySQL.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 11-Mar-2013
 */
public class UsersDatasetToMySQL {

    public static final String CORE_FEATURES_TABLE_NAME = "user_features";
    public static final String CORE_FEATURES_COLUMN_NAME = "name";
    public static final String CORE_FEATURES_COLUMN_TYPE = "type";
    public static final String CORE_USERS_TABLE_NAME = "users";
    public static final String CORE_USERS_COLUMN_ID = "iduser";
    public static final String CORE_USERS_COLUMN_NAME = "name";

    private final String varcharSize = "200";
    private final String intSize = "11";

    private final String featureTableName;
    private final String featureTable_featureName, featureTable_featureType;

    private final String usersTableName;
    private final String usersTable_idUser, usersTable_userName;

    private final MySQLConnection mySQLConnection;

    public UsersDatasetToMySQL(MySQLConnection mySQLConnection) {
        this.mySQLConnection = mySQLConnection;

        featureTableName = mySQLConnection.getPrefix() + CORE_FEATURES_TABLE_NAME;

        featureTable_featureName = CORE_FEATURES_COLUMN_NAME;
        featureTable_featureType = CORE_FEATURES_COLUMN_TYPE;

        usersTableName = mySQLConnection.getPrefix() + CORE_USERS_TABLE_NAME;
        usersTable_idUser = CORE_USERS_COLUMN_ID;
        usersTable_userName = CORE_USERS_COLUMN_NAME;

    }

    public void writeDataset(UsersDataset usersDataset) throws SQLException {

        createTables(usersDataset);
        Feature[] features = usersDataset.getFeatures();
        StringBuilder beginingOfInsert = new StringBuilder();
        {
            beginingOfInsert.append("INSERT INTO ");
            beginingOfInsert.append(usersTableName);
            beginingOfInsert.append(" (");
            beginingOfInsert.append(usersTable_idUser);
            beginingOfInsert.append(",");
            beginingOfInsert.append(usersTable_userName);
            for (Feature userFeature : features) {
                beginingOfInsert.append(",").append(userFeature.getName());

            }
            beginingOfInsert.append(") VALUES (");

        }

        for (User user : usersDataset) {
            int idUser = user.getId();
            StringBuilder insert = new StringBuilder();
            insert.append(beginingOfInsert);

            //Escribir el id y el nombre
            insert.append(idUser);
            insert.append(",'");
            insert.append(user.getName());
            insert.append("'");

            for (Feature userFeature : features) {
                insert.append(",");
                switch (userFeature.getType()) {
                    case Nominal:
                        insert.append("'");
                        insert.append(user.getFeatureValue(userFeature).toString());
                        insert.append("'");
                        break;
                    case Numerical:
                        insert.append(user.getFeatureValue(userFeature).toString());
                        break;
                    default:
                        insert.append("'");
                        insert.append(user.getFeatureValue(userFeature).toString());
                        insert.append("'");
                        break;
                }
            }
            insert.append(");");

            mySQLConnection.execute(insert.toString());
        }
        mySQLConnection.commit();
    }

    public UsersDataset readDataset() throws SQLException {

        FeatureGenerator featureGenerator = new FeatureGenerator();
        TreeSet<User> users = new TreeSet<>();

        //Leo las características.
        ResultSet rstFeatures = mySQLConnection.executeQuery("SELECT " + featureTable_featureName + "," + featureTable_featureType + " "
                + "FROM " + featureTableName + ";");

        while (rstFeatures.next()) {
            String featureName = rstFeatures.getString(1);
            String type = rstFeatures.getString(2);
            FeatureType featureType = FeatureType.getFeatureType(type);

            if (!featureGenerator.containsFeature(featureName)) {
                featureGenerator.createFeature(featureName, featureType);
            }

        }

        //Leo los usuarios
        StringBuilder consulta = new StringBuilder();

        consulta.append(
                "SELECT ").append(usersTable_idUser).append(",").append(usersTable_userName);

        for (Feature f
                : featureGenerator.getSortedFeatures()) {
            consulta.append(',');
            consulta.append(f.getName());
        }

        consulta.append(
                " FROM ").append(usersTableName).append(";");

        ResultSet rst = mySQLConnection.executeQuery(consulta.toString());

        while (rst.next()) {
            int idUser = rst.getInt(1);
            String name = rst.getString(2);

            int index = 0;

            Object[] values = new Object[featureGenerator.getSortedFeatures().size()];
            for (Feature userFeature : featureGenerator.getSortedFeatures()) {
                switch (userFeature.getType()) {
                    case Nominal:
                        values[index] = rst.getString(index + 3);
                        break;
                    case Numerical:
                        values[index] = rst.getDouble(index + 3);
                        break;
                    default:
                        throw new UnsupportedOperationException("User attribute type '" + userFeature.getType() + "' not supported yet.");
                }
                index++;
            }
            users.add(new User(
                    idUser,
                    name,
                    featureGenerator.getSortedFeatures().toArray(new Feature[0]),
                    values));
        }

        return new UsersDatasetAdapter(users);

    }

    private void createTables(UsersDataset usersDataset) throws SQLException {

        deleteTables();

        Feature[] userFeatures = usersDataset.getFeatures();

        String createFeaturesTable = "CREATE TABLE " + featureTableName + "("
                + featureTable_featureName + " VARCHAR(" + varcharSize + ") NOT NULL,"
                + featureTable_featureType + " VARCHAR(" + varcharSize + ") NOT NULL,"
                + "PRIMARY KEY(" + featureTable_featureName + "));";

        mySQLConnection.execute(createFeaturesTable);

        for (Feature userFeature : userFeatures) {
            final String insertFeature = "INSERT INTO " + featureTableName + " "
                    + "(" + featureTable_featureName
                    + "," + featureTable_featureType + ") "
                    + "VALUES ('" + userFeature.getName().replaceAll("'", "''") + "','" + userFeature.getType().name() + "');";
            mySQLConnection.execute(insertFeature);
        }

        StringBuilder createContentTable = new StringBuilder();

        createContentTable.append("CREATE TABLE `");
        createContentTable.append(usersTableName);
        createContentTable.append("` (\n");
        createContentTable.append("\t").append("`").append(usersTable_idUser).append("` INT(" + intSize + ") NOT NULL,\n");
        createContentTable.append("\t").append("`").append(usersTable_userName).append("` VARCHAR(" + varcharSize + ") DEFAULT NULL,\n");

        for (Feature userFeature : userFeatures) {

            createContentTable.append("\t").append("`").append(userFeature.getName()).append("`");
            switch (userFeature.getType()) {
                case Nominal:
                    createContentTable.append(" VARCHAR(" + varcharSize + ") DEFAULT NULL,");
                    break;
                case Numerical:
                    createContentTable.append(" FLOAT DEFAULT NULL,");
                    break;
                default:
                    throw new IllegalArgumentException("Not implemented for '" + userFeature.getType() + "'");
            }

            createContentTable.append("\n");
        }

        createContentTable.append("PRIMARY KEY (");
        createContentTable.append(usersTable_idUser);
        createContentTable.append(") );");

        mySQLConnection.execute(createContentTable.toString());
    }

    private void deleteTables() throws SQLException {
        mySQLConnection.execute("DROP TABLE IF EXISTS " + usersTableName + ";");
        mySQLConnection.execute("DROP TABLE IF EXISTS " + featureTableName + ";");
    }
}
