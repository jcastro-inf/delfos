package delfos.main.managers.database;

import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import delfos.constants.DelfosTest;
import delfos.constants.TestConstants;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.loaders.database.mysql.changeable.ChangeableMySQLDatasetLoader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Granada, sci2s)
 * <jorgecastrog@correo.ugr.es>
 */
public class DatabaseManagerMySQLTest extends DelfosTest {

    /**
     * Directorio en el que se almacenan los ficheros temporales.
     */
    private final static String manageDataset_directory = TestConstants.TEST_DATA_DIRECTORY
            + DatabaseManagerMySQLTest.class.getSimpleName() + File.separator;
    /**
     * Nombre del fichero que almacena la configuración del dataset manejado por
     * la biblioteca.
     */
    private final static File manageDatasetConfigFile = new File(manageDataset_directory + "mysqlDatasetConfiguration.xml");
    private static MySQLConnection mySQLConnection;

    private void initConnection() {
        try {
            mySQLConnection = new MySQLConnection(
                    TestConstants.databaseTestUserName,
                    TestConstants.databaseTestPassword,
                    TestConstants.databaseTestDatabaseName,
                    TestConstants.databaseTestHost,
                    TestConstants.databaseTestPort,
                    "test_");
        } catch (ClassNotFoundException | SQLException ex) {
            Global.showWarning("Database not ready.");
            Global.showError(ex);
            mySQLConnection = null;
            Assert.fail("Database not ready.");
        }
    }

    private void checkDatabase() throws SQLException {
        if (mySQLConnection == null) {
            Assert.fail("Database not ready.");
        } else {
            Connection connection = mySQLConnection.doConnection();
        }
    }

    private void cleanDatabase() throws SQLException {

        //Drop tables
        try (Connection connection = mySQLConnection.doConnection(); Statement statement = connection.createStatement()) {

            ChangeableMySQLDatasetLoader datasetLoader = new ChangeableMySQLDatasetLoader(mySQLConnection);

            String dropTable_itemFeatures = "Drop table if exists " + datasetLoader.getContentDefinitionTable_name_withPrefix() + ";";
            statement.execute(dropTable_itemFeatures);

            String dropTable_Items = "Drop table if exists " + datasetLoader.getProductsTable_name_withPrefix() + ";";
            statement.execute(dropTable_Items);

            String dropTable_UserFeatures = "Drop table if exists " + datasetLoader.getUserFeaturesDefinitionTable_name_withPrefix() + ";";
            statement.execute(dropTable_UserFeatures);

            String dropTable_Users = "Drop table if exists " + datasetLoader.getUsersTable_name_withPrefix() + ";";
            statement.execute(dropTable_Users);

            String dropTable_Ratings = "Drop table if exists " + datasetLoader.getRatingsTable_name_withPrefix() + ";";
            statement.execute(dropTable_Ratings);

            statement.execute("Commit;");

            Global.showInfoMessage("Tables dropped.\n");

        }
    }

    @Before
    public void setUp() throws SQLException, IOException {

        File directoryFile = new File(manageDataset_directory);

        FileUtilities.cleanDirectory(directoryFile);

        initConnection();
        checkDatabase();
        cleanDatabase();

        ChangeableMySQLDatasetLoader datasetLoader = new ChangeableMySQLDatasetLoader(mySQLConnection);

        ChangeableDatasetConfigurationFileParser.saveConfigFile(
                manageDatasetConfigFile,
                datasetLoader);
    }

    @Test
    public void testManageDataset_CSV_DatabaseInitialisation() throws Exception {
        DatabaseManagerTestSuite.initDatabase(manageDatasetConfigFile);
    }

    @Test
    public void testManageDataset_CSV_addUser() throws Exception {

        int idUser = 15;
        DatabaseManagerTestSuite.initDatabase(manageDatasetConfigFile);

        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, idUser);
    }

    @Test
    public void testManageDataset_CSV_addUserFeatures() throws Exception {

        int idUser = 15;
        DatabaseManagerTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, idUser);

        DatabaseManagerTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");
    }

    @Test
    public void testManageDataset_CSV_addItem() throws Exception {

        int idUser = 15;
        int idItem = 23;
        DatabaseManagerTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, idUser);
        DatabaseManagerTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");

        DatabaseManagerTestSuite.addItem(manageDatasetConfigFile, idItem);
    }

    @Test
    public void testManageDataset_CSV_addItemFeatures() throws Exception {

        int idUser = 15;
        int idItem = 23;
        DatabaseManagerTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, idUser);
        DatabaseManagerTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");
        DatabaseManagerTestSuite.addItem(manageDatasetConfigFile, idItem);

        DatabaseManagerTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:Película de drama",
                "año_numerical:1959",
                "pais_nominal:España");
        DatabaseManagerTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:PeliDeDramaTest_NewName",
                "duración_numerical:94",
                "género_nominal:Drama");
    }

    @Test
    public void testManageDataset_CSV_addRating() throws Exception {
        int idUser = 15;
        int idItem = 23;
        double ratingValue = 4.5;

        DatabaseManagerTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, idUser);
        DatabaseManagerTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");
        DatabaseManagerTestSuite.addItem(manageDatasetConfigFile, idItem);
        DatabaseManagerTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:Película de drama",
                "año_numerical:1959",
                "pais_nominal:España");
        DatabaseManagerTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:PeliDeDramaTest_NewName",
                "duración_numerical:94",
                "género_nominal:Drama");

        DatabaseManagerTestSuite.addRating(manageDatasetConfigFile, idUser, idItem, ratingValue);
    }

    @Test
    public void testManageDataset_CSV_Printer() throws Exception {
        int idUser = 15;
        int idItem = 23;
        double ratingValue = 4.5;

        DatabaseManagerTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, idUser);
        DatabaseManagerTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");
        DatabaseManagerTestSuite.addItem(manageDatasetConfigFile, idItem);
        DatabaseManagerTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:Película de drama",
                "año_numerical:1959",
                "pais_nominal:España");
        DatabaseManagerTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:PeliDeDramaTest_NewName",
                "duración_numerical:94",
                "género_nominal:Drama");

        DatabaseManagerTestSuite.addRating(manageDatasetConfigFile, idUser, idItem, ratingValue);

        //Ratings del usuario 15
        DatabaseManagerTestSuite.addItem(manageDatasetConfigFile, 59);
        DatabaseManagerTestSuite.addItem(manageDatasetConfigFile, 24);
        DatabaseManagerTestSuite.addRating(manageDatasetConfigFile, idUser, 59, 4.15);
        DatabaseManagerTestSuite.addRating(manageDatasetConfigFile, idUser, 24, 1.59);

        //Ratings del item 23
        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, 11);
        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, 16);
        DatabaseManagerTestSuite.addUser(manageDatasetConfigFile, 19);
        DatabaseManagerTestSuite.addRating(manageDatasetConfigFile, 11, idItem, 3);
        DatabaseManagerTestSuite.addRating(manageDatasetConfigFile, 16, idItem, 2);
        DatabaseManagerTestSuite.addRating(manageDatasetConfigFile, 19, idItem, 3.8);

        DatabaseManagerTestSuite.printUsers(manageDatasetConfigFile);
        DatabaseManagerTestSuite.printItems(manageDatasetConfigFile);
        DatabaseManagerTestSuite.printUserRatings(manageDatasetConfigFile, idUser);
        DatabaseManagerTestSuite.printItemRatings(manageDatasetConfigFile, idItem);
    }

}
