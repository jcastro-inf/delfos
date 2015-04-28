package delfos.main.managers.database.old;

import java.io.File;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import delfos.common.FileUtilities;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import delfos.constants.DelfosTest;
import delfos.constants.TestConstants;
import delfos.dataset.loaders.csv.changeable.ChangeableCSVFileDatasetLoader;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Granada, sci2s)
 * <jorgecastrog@correo.ugr.es>
 */
public class DatabaseManagerOldParametersTest extends DelfosTest {

    /**
     * Directorio en el que se almacenan los ficheros temporales.
     */
    private final static String manageDataset_directory
            = TestConstants.TEST_DATA_DIRECTORY
            + DatabaseManagerOldParametersTest.class.getSimpleName() + File.separator;
    /**
     * Nombre del fichero que almacena la configuración del dataset manejado por
     * la biblioteca.
     */
    private final static File manageDatasetConfigFile = new File(
            manageDataset_directory
            + DatabaseManagerOldParametersTest.class.getSimpleName() + "_Configuration.xml");

    private final static File ratingsFile = new File(
            manageDataset_directory + "changeableRatings.csv");
    private final static File contentFile = new File(
            manageDataset_directory + "changeableContent.csv");
    private final static File usersFile = new File(
            manageDataset_directory + "changeableUsers.csv");

    @Before
    public void setUp() {
        File directoryFile_ManageDataset = new File(manageDataset_directory);

        FileUtilities.cleanDirectory(directoryFile_ManageDataset);

        ChangeableCSVFileDatasetLoader datasetLoader_ManageDataset = new ChangeableCSVFileDatasetLoader(
                ratingsFile.getPath(),
                contentFile.getPath(),
                usersFile.getPath());

        ChangeableDatasetConfigurationFileParser.saveConfigFile(
                manageDatasetConfigFile.getPath(),
                datasetLoader_ManageDataset);
    }

    @Test
    public void testManageDataset_CSV_DatabaseInitialisation() throws Exception {

        DatabaseManagerOldParametersTestSuite.initDatabase(manageDatasetConfigFile);
        assertTrue("The ratings file does not exists", ratingsFile.exists());
        assertTrue("The content file does not exists", contentFile.exists());
        assertTrue("The users file does not exists", usersFile.exists());
    }

    @Test
    public void testManageDataset_CSV_addUser() throws Exception {

        int idUser = 15;
        DatabaseManagerOldParametersTestSuite.initDatabase(manageDatasetConfigFile);

        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, idUser);
    }

    @Test
    public void testManageDataset_CSV_addUserFeatures() throws Exception {

        int idUser = 15;
        DatabaseManagerOldParametersTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, idUser);

        DatabaseManagerOldParametersTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");
    }

    @Test
    public void testManageDataset_CSV_addItem() throws Exception {
        int idUser = 15;
        int idItem = 23;
        DatabaseManagerOldParametersTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, idUser);
        DatabaseManagerOldParametersTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");

        DatabaseManagerOldParametersTestSuite.addItem(manageDatasetConfigFile, idItem);
    }

    @Test
    public void testManageDataset_CSV_addItemFeatures() throws Exception {

        int idUser = 15;
        int idItem = 23;
        DatabaseManagerOldParametersTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, idUser);
        DatabaseManagerOldParametersTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");
        DatabaseManagerOldParametersTestSuite.addItem(manageDatasetConfigFile, idItem);

        DatabaseManagerOldParametersTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:Película de drama",
                "año_numerical:1959",
                "pais_nominal:España");
        DatabaseManagerOldParametersTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:PeliDeDramaTest_NewName",
                "duración_numerical:94",
                "género_nominal:Drama");
    }

    @Test
    public void testManageDataset_CSV_addRating() throws Exception {
        int idUser = 15;
        int idItem = 23;
        double ratingValue = 4.5;

        DatabaseManagerOldParametersTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, idUser);
        DatabaseManagerOldParametersTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");
        DatabaseManagerOldParametersTestSuite.addItem(manageDatasetConfigFile, idItem);
        DatabaseManagerOldParametersTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:Película de drama",
                "año_numerical:1959",
                "pais_nominal:España");
        DatabaseManagerOldParametersTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:PeliDeDramaTest_NewName",
                "duración_numerical:94",
                "género_nominal:Drama");

        DatabaseManagerOldParametersTestSuite.addRating(manageDatasetConfigFile, idUser, idItem, ratingValue);

    }

    @Test
    public void testManageDataset_CSV_Printer() throws Exception {
        int idUser = 15;
        int idItem = 23;
        double ratingValue = 4.5;

        DatabaseManagerOldParametersTestSuite.initDatabase(manageDatasetConfigFile);
        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, idUser);
        DatabaseManagerOldParametersTestSuite.addUserFeatures(manageDatasetConfigFile, idUser,
                "name:juanita",
                "edad_numerical:59",
                "pais_nominal:España");
        DatabaseManagerOldParametersTestSuite.addItem(manageDatasetConfigFile, idItem);
        DatabaseManagerOldParametersTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:Película de drama",
                "año_numerical:1959",
                "pais_nominal:España");
        DatabaseManagerOldParametersTestSuite.addItemFeatures(manageDatasetConfigFile, idItem,
                "name:PeliDeDramaTest_NewName",
                "duración_numerical:94",
                "género_nominal:Drama");

        DatabaseManagerOldParametersTestSuite.addRating(manageDatasetConfigFile, idUser, idItem, ratingValue);

        //Ratings del usuario 15
        DatabaseManagerOldParametersTestSuite.addItem(manageDatasetConfigFile, 59);
        DatabaseManagerOldParametersTestSuite.addItem(manageDatasetConfigFile, 24);
        DatabaseManagerOldParametersTestSuite.addRating(manageDatasetConfigFile, idUser, 59, 4.15);
        DatabaseManagerOldParametersTestSuite.addRating(manageDatasetConfigFile, idUser, 24, 1.59);

        //Ratings del item 23
        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, 11);
        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, 16);
        DatabaseManagerOldParametersTestSuite.addUser(manageDatasetConfigFile, 19);
        DatabaseManagerOldParametersTestSuite.addRating(manageDatasetConfigFile, 11, idItem, 3);
        DatabaseManagerOldParametersTestSuite.addRating(manageDatasetConfigFile, 16, idItem, 2);
        DatabaseManagerOldParametersTestSuite.addRating(manageDatasetConfigFile, 19, idItem, 3.8);

        DatabaseManagerOldParametersTestSuite.printUsers(manageDatasetConfigFile);
        DatabaseManagerOldParametersTestSuite.printItems(manageDatasetConfigFile);
        DatabaseManagerOldParametersTestSuite.printUserRatings(manageDatasetConfigFile, idUser);
        DatabaseManagerOldParametersTestSuite.printItemRatings(manageDatasetConfigFile, idItem);

    }

}
