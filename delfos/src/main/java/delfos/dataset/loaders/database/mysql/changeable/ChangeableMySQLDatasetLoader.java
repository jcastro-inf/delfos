package delfos.dataset.loaders.database.mysql.changeable;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.StringParameter;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.changeable.ChangeableDatasetLoaderAbstract;
import delfos.dataset.changeable.ChangeableUsersDataset;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_DATABASE_NAME;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_HOST_NAME;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_PASSWORD;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_PORT;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_PREFIX;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_USER;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Dataset almacenado en CSV que permite la modificación de sus datos.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public class ChangeableMySQLDatasetLoader extends ChangeableDatasetLoaderAbstract {

    private static final long serialVersionUID = 1L;//Tabla RATINGS
    //Conexión usada.
    //Tabla de Ratings
    public static final Parameter ratingsTable_name = new Parameter("ratingsTable_name", new StringParameter("ratings"));
    public static final Parameter ratingsTable_UserIDField = new Parameter("ratingsTable_UserIDField", new StringParameter("iduser"));
    public static final Parameter ratingsTable_ItemIDField = new Parameter("ratingsTable_ItemIDField", new StringParameter("iditem"));
    public static final Parameter ratingsTable_RatingField = new Parameter("ratingsTable_RatingField", new StringParameter("rating"));
    public static final Parameter ratingsTable_TimestampField = new Parameter("ratingsTable_TimestampField", new StringParameter("timestamp"));
    //Tabla de definición de características de los productos. Define los atributos, nombre de la columna y tipo.
    public static final Parameter contentDefinitionTable_name = new Parameter("contentDefinitionTable_name", new StringParameter("itemfeatures"));
    public static final Parameter contentDefinitionTable_FeatureIdField = new Parameter("contentDefinitionTable_FeatureIdField", new StringParameter("id_feature"));
    public static final Parameter contentDefinitionTable_FeatureNameField = new Parameter("contentDefinitionTable_FeatureNameField", new StringParameter("name"));
    public static final Parameter contentDefinitionTable_FeatureTypeField = new Parameter("contentDefinitionTable_FeatureTypeField", new StringParameter("type"));
    //Tabla de productos
    public static final Parameter productsTable_name = new Parameter("productsTable_name", new StringParameter("items"));
    public static final Parameter productsTable_ItemIDField = new Parameter("productsTable_ItemIDField", new StringParameter("id_producto"));
    public static final Parameter productsTable_NameField = new Parameter("productsTable_NameField", new StringParameter("name"));
    public static final Parameter productsTable_AvailabilityField = new Parameter("productsTable_AvailabilityField", new StringParameter("available"));
    //Tabla de definición de características de los usuarios. Define los atributos, nombre de la columna y tipo.
    public static final Parameter userFeaturesDefinitionTable_name = new Parameter("userFeaturesDefinitionTable_name", new StringParameter("userfeatures"));
    public static final Parameter userFeaturesDefinitionTable_FeatureIdField = new Parameter("userFeaturesDefinitionTable_FeatureIdField", new StringParameter("id_feature"));
    public static final Parameter userFeaturesTable_FeatureNameField = new Parameter("userFeaturesTable_FeatureNameField", new StringParameter("name"));
    public static final Parameter userFeaturesTable_FeatureTypeField = new Parameter("userFeaturesTable_FeatureTypeField", new StringParameter("type"));
    //Tabla de usuarios
    public static final Parameter usersTable_name = new Parameter("usersTable_name", new StringParameter("users"));
    public static final Parameter usersTable_UserIDField = new Parameter("usersTable_UserIDField", new StringParameter("id_user"));
    public static final Parameter usersTable_NameField = new Parameter("usersTable_NameField", new StringParameter("name"));
    /*
     * Atributos para el almacenamiento en memoria de los datasets.
     */
    private ChangeableMySQLRatingsDataset ratingsDataset;
    private ChangeableMySQLContentDataset contentDataset;
    private ChangeableMySQLUsersDataset usersDataset;

    public ChangeableMySQLDatasetLoader() {
        super();

        //Database configuration parameters
        addParameter(CONNECTION_CONFIGURATION_USER);
        addParameter(CONNECTION_CONFIGURATION_PASSWORD);
        addParameter(CONNECTION_CONFIGURATION_DATABASE_NAME);
        addParameter(CONNECTION_CONFIGURATION_HOST_NAME);
        addParameter(CONNECTION_CONFIGURATION_PORT);
        addParameter(CONNECTION_CONFIGURATION_PREFIX);

        //Tabla de ratings
        addParameter(ratingsTable_name);
        addParameter(ratingsTable_UserIDField);
        addParameter(ratingsTable_ItemIDField);
        addParameter(ratingsTable_RatingField);
        addParameter(ratingsTable_TimestampField);

        //Tabla de características de los productos
        addParameter(contentDefinitionTable_name);
        addParameter(contentDefinitionTable_FeatureIdField);
        addParameter(contentDefinitionTable_FeatureNameField);
        addParameter(contentDefinitionTable_FeatureTypeField);

        //Tabla PRODUCTOS Y CONTENIDO
        addParameter(productsTable_name);
        addParameter(productsTable_ItemIDField);
        addParameter(productsTable_AvailabilityField);
        addParameter(productsTable_NameField);

        //Tabla de definición de características de los usuarios. Define los atributos, nombre de la columna y tipo.
        addParameter(userFeaturesDefinitionTable_name);
        addParameter(userFeaturesDefinitionTable_FeatureIdField);
        addParameter(userFeaturesTable_FeatureNameField);
        addParameter(userFeaturesTable_FeatureTypeField);
        //Tabla de usuarios
        addParameter(usersTable_name);
        addParameter(usersTable_UserIDField);
        addParameter(usersTable_NameField);

        addParammeterListener(() -> {
            ratingsDataset = null;
            contentDataset = null;
            usersDataset = null;
        });
    }

    public ChangeableMySQLDatasetLoader(MySQLConnection connection) {
        this();

        setParameterValue(CONNECTION_CONFIGURATION_USER, connection.getUser());
        setParameterValue(CONNECTION_CONFIGURATION_PASSWORD, connection.getPass());
        setParameterValue(CONNECTION_CONFIGURATION_DATABASE_NAME, connection.getDatabaseName());
        setParameterValue(CONNECTION_CONFIGURATION_HOST_NAME, connection.getHostName());
        setParameterValue(CONNECTION_CONFIGURATION_PORT, connection.getPort());
        setParameterValue(CONNECTION_CONFIGURATION_PREFIX, connection.getPrefix());
    }

    public synchronized final MySQLConnection getMySQLConnectionDescription() throws SQLException {
        try {
            String _user = (String) getParameterValue(CONNECTION_CONFIGURATION_USER);
            String _pass = (String) getParameterValue(CONNECTION_CONFIGURATION_PASSWORD);
            String _databaseName = (String) getParameterValue(CONNECTION_CONFIGURATION_DATABASE_NAME);
            String _serverName = (String) getParameterValue(CONNECTION_CONFIGURATION_HOST_NAME);
            int _port = (Integer) getParameterValue(CONNECTION_CONFIGURATION_PORT);

            String _prefix = (String) getParameterValue(CONNECTION_CONFIGURATION_PREFIX);

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage(ChangeableMySQLDatasetLoader.class + ": Making database connection with properties:\n");

                Global.showInfoMessage("\tUser -----------> " + _user + "\n");
                Global.showInfoMessage("\tPass -----------> " + _pass + "\n");
                Global.showInfoMessage("\tDatabase Name --> " + _databaseName + "\n");
                Global.showInfoMessage("\tServer Name ----> " + _serverName + "\n");
                Global.showInfoMessage("\tPort -----------> " + _port + "\n");
                Global.showInfoMessage("\tPrefix ---------> " + _prefix + "\n");
            }

            return new MySQLConnection(_user, _pass, _databaseName, _serverName, _port, _prefix);
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public synchronized ChangeableMySQLRatingsDataset getChangeableRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                ratingsDataset = new ChangeableMySQLRatingsDataset(
                        getMySQLConnectionDescription(),
                        getRatingsTable_name(),
                        getRatingsTable_UserIDField(),
                        getRatingsTable_ItemIDField(),
                        getRatingsTable_RatingField(),
                        getRatingsTable_TimestampField());
            } catch (SQLException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }
        return ratingsDataset;
    }

    @Override
    public synchronized ChangeableMySQLContentDataset getChangeableContentDataset() throws CannotLoadContentDataset {
        if (contentDataset == null) {

            try {
                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage(ChangeableMySQLDatasetLoader.class + ": Loading content dataset with properties\n");

                    Global.showInfoMessage("\tFeatures table name --> " + getContentDefinitionTable_name() + "\n");
                    Global.showInfoMessage("\t      feature id -----> " + getContentDefinitionTable_FeatureIdField() + "\n");
                    Global.showInfoMessage("\t      feature name ---> " + getContentDefinitionTable_FeatureNameField() + "\n");
                    Global.showInfoMessage("\t      feature type ---> " + getContentDefinitionTable_FeatureTypeField() + "\n");
                    Global.showInfoMessage("\tItem table name-------> " + getProductsTable_name() + "\n");
                    Global.showInfoMessage("\t      item id --------> " + getProductsTable_ItemIDField() + "\n");
                    Global.showInfoMessage("\t      item name ------> " + getProductsTable_NameField() + "\n");
                    Global.showInfoMessage("\t      item avail.? ---> " + getProductsTable_AvailabilityField() + "\n");
                }

                contentDataset = new ChangeableMySQLContentDataset(
                        getMySQLConnectionDescription(),
                        getContentDefinitionTable_name(),
                        getContentDefinitionTable_FeatureIdField(),
                        getContentDefinitionTable_FeatureNameField(),
                        getContentDefinitionTable_FeatureTypeField(),
                        getProductsTable_name(),
                        getProductsTable_ItemIDField(),
                        getProductsTable_NameField(),
                        getProductsTable_AvailabilityField());
            } catch (SQLException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return contentDataset;
    }

    @Override
    public synchronized ChangeableUsersDataset getChangeableUsersDataset() throws CannotLoadUsersDataset {
        if (usersDataset == null) {

            try {
                usersDataset = new ChangeableMySQLUsersDataset(
                        getMySQLConnectionDescription(),
                        getUserFeaturesDefinitionTable_name(),
                        getUserFeaturesDefinitionTable_FeatureIdField(),
                        getUserFeaturesTable_FeatureNameField(),
                        getUserFeaturesTable_FeatureTypeField(),
                        getUsersTable_name(),
                        getUsersTable_UserIDField(),
                        getUsersTable_NameField());
            } catch (SQLException ex) {
                throw new CannotLoadUsersDataset(ex);
            }
        }
        return usersDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria();
    }

    @Override
    public synchronized void initStructures() {

        if (!Global.askUser("This cannot be undone: Are you sure you want to delete the previous database of ratings? ")) {
            IllegalStateException ex = new IllegalStateException("Operation aborted by user.");
            ERROR_CODES.OPERATION_ABORTED_BY_USER.exit(ex);
        }

        //Drop all previous tables.
        //Drop tables
        try (
                Statement statement = getMySQLConnectionDescription().doConnection().createStatement()) {

            String dropTable_itemFeatures = "Drop table if exists " + getContentDefinitionTable_name_withPrefix() + ";";
            statement.execute(dropTable_itemFeatures);

            String dropTable_Items = "Drop table if exists " + getProductsTable_name_withPrefix() + ";";
            statement.execute(dropTable_Items);

            String dropTable_UserFeatures = "Drop table if exists " + getUserFeaturesDefinitionTable_name_withPrefix() + ";";
            statement.execute(dropTable_UserFeatures);

            String dropTable_Users = "Drop table if exists " + getUsersTable_name_withPrefix() + ";";
            statement.execute(dropTable_Users);

            String dropTable_Ratings = "Drop table if exists " + getRatingsTable_name_withPrefix() + ";";
            statement.execute(dropTable_Ratings);

            statement.execute("Commit;");

            Global.showInfoMessage("Tables dropped.");

        } catch (SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        try {
            ratingsDataset = new ChangeableMySQLRatingsDataset(
                    getMySQLConnectionDescription(),
                    getRatingsTable_name(),
                    getRatingsTable_UserIDField(),
                    getRatingsTable_ItemIDField(),
                    getRatingsTable_RatingField(),
                    getRatingsTable_TimestampField());
            ratingsDataset.createTables();
        } catch (SQLException ex) {
            Global.showWarning("Problems at initialisating ratings dataset.");
        }

        try {
            contentDataset = new ChangeableMySQLContentDataset(
                    getMySQLConnectionDescription(),
                    getContentDefinitionTable_name(),
                    getContentDefinitionTable_FeatureIdField(),
                    getContentDefinitionTable_FeatureNameField(),
                    getContentDefinitionTable_FeatureTypeField(),
                    getProductsTable_name(),
                    getProductsTable_ItemIDField(),
                    getProductsTable_NameField(),
                    getProductsTable_AvailabilityField());
            contentDataset.createTables();
        } catch (SQLException ex) {
            Global.showWarning("Problems at initialisating content dataset.");
        }

        try {
            usersDataset = new ChangeableMySQLUsersDataset(
                    getMySQLConnectionDescription(),
                    getUserFeaturesDefinitionTable_name(),
                    getUserFeaturesDefinitionTable_FeatureIdField(),
                    getUserFeaturesTable_FeatureNameField(),
                    getUserFeaturesTable_FeatureTypeField(),
                    getUsersTable_name(),
                    getUsersTable_UserIDField(),
                    getUsersTable_NameField());
            usersDataset.createTables();
        } catch (SQLException ex) {
            Global.showWarning("Problems at initialisating users dataset.");
        }

    }

    //======= Declaro los métodos para obtener valores de la tabla ratings =====
    /**
     * Devuelve el nombre de la tabla que almacena las valoraciones.
     *
     * @return Tabla que almacena las valoraciones.
     */
    private String getRatingsTable_name() {
        return (String) getParameterValue(ratingsTable_name);
    }

    /**
     * Devuelve el campo que almacena el idItem en la tabla de valoraciones.
     *
     * @return Campo del producto.
     */
    public String getRatingsTable_UserIDField() {
        return (String) getParameterValue(ratingsTable_UserIDField);
    }

    /**
     * Devuelve el campo que almacena el idItem en la tabla de valoraciones.
     *
     * @return Campo del producto.
     */
    public String getRatingsTable_ItemIDField() {
        return (String) getParameterValue(ratingsTable_ItemIDField);
    }

    /**
     * Devuelve el campo que almacena la valoración en la tabla de valoraciones.
     *
     * @return Campo de la valoración.
     */
    public String getRatingsTable_RatingField() {
        return (String) getParameterValue(ratingsTable_RatingField);
        //return "review_rating";
    }

    /**
     * Devuelve el campo que almacena la valoración en la tabla de valoraciones.
     *
     * @return Campo de la valoración.
     */
    public String getRatingsTable_TimestampField() {
        return (String) getParameterValue(ratingsTable_TimestampField);
        //return "review_rating";
    }

    public String getContentDefinitionTable_FeatureIdField() {
        return (String) getParameterValue(contentDefinitionTable_FeatureIdField);
    }

    public String getContentDefinitionTable_FeatureNameField() {
        return (String) getParameterValue(contentDefinitionTable_FeatureNameField);
    }

    public String getContentDefinitionTable_FeatureTypeField() {
        return (String) getParameterValue(contentDefinitionTable_FeatureTypeField);
    }

    public String getProductsTable_ItemIDField() {
        return (String) getParameterValue(productsTable_ItemIDField);
    }

    public String getProductsTable_AvailabilityField() {
        return (String) getParameterValue(productsTable_AvailabilityField);
    }

    private String getProductsTable_NameField() {
        return (String) getParameterValue(productsTable_NameField);
    }

    private String getProductsTable_name() {
        return (String) getParameterValue(productsTable_name);
    }

    private String getContentDefinitionTable_name() {
        return (String) getParameterValue(contentDefinitionTable_name);
    }

    public String getContentDefinitionTable_name_withPrefix() {
        return getConnectionPrefix() + getContentDefinitionTable_name();
    }

    public String getProductsTable_name_withPrefix() {
        return getConnectionPrefix() + getProductsTable_name();
    }

    public String getRatingsTable_name_withPrefix() {
        return getConnectionPrefix() + getRatingsTable_name();
    }

    private String getUserFeaturesDefinitionTable_name() {
        return (String) getParameterValue(userFeaturesDefinitionTable_name);
    }

    public String getUserFeaturesDefinitionTable_name_withPrefix() {
        return getConnectionPrefix() + getUserFeaturesDefinitionTable_name();
    }

    private String getUserFeaturesDefinitionTable_FeatureIdField() {
        return (String) getParameterValue(userFeaturesDefinitionTable_FeatureIdField);
    }

    private String getUserFeaturesTable_FeatureNameField() {
        return (String) getParameterValue(userFeaturesTable_FeatureNameField);
    }

    private String getUserFeaturesTable_FeatureTypeField() {
        return (String) getParameterValue(userFeaturesTable_FeatureTypeField);
    }

    private String getUsersTable_name() {
        return (String) getParameterValue(usersTable_name);
    }

    public String getUsersTable_name_withPrefix() {
        return getConnectionPrefix() + getUsersTable_name();
    }

    private String getUsersTable_UserIDField() {
        return (String) getParameterValue(usersTable_UserIDField);
    }

    private String getUsersTable_NameField() {
        return (String) getParameterValue(usersTable_NameField);
    }

    public String getConnectionPrefix() {
        return (String) getParameterValue(CONNECTION_CONFIGURATION_PREFIX);

    }
}
