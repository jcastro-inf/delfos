package delfos.main.managers.database;

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
public class DatabaseManagerCSVTest extends DelfosTest {

    /**
     * Directorio en el que se almacenan los ficheros temporales.
     */
    private final static String manageDataset_folder
            = TestConstants.TEST_DATA_DIRECTORY
            + DatabaseManagerCSVTest.class.getSimpleName() + File.separator;
    /**
     * Nombre del fichero que almacena la configuración del dataset manejado por
     * la biblioteca.
     */
    private final static File manageDatasetConfigFile = new File(
            manageDataset_folder
            + DatabaseManagerCSVTest.class.getSimpleName() + "_Configuration.xml");

    private final static File ratingsFile = new File(
            manageDataset_folder + "changeableRatings.csv");
    private final static File contentFile = new File(
            manageDataset_folder + "changeableContent.csv");
    private final static File usersFile = new File(
            manageDataset_folder + "changeableUsers.csv");

    @Before
    public void setUp() {
        File folderFile_ManageDataset = new File(manageDataset_folder);

        FileUtilities.cleanDirectory(folderFile_ManageDataset);

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

        DatabaseManagerTestSuite.initDatabase(manageDatasetConfigFile);
        assertTrue("The ratings file does not exists", ratingsFile.exists());
        assertTrue("The content file does not exists", contentFile.exists());
        assertTrue("The users file does not exists", usersFile.exists());
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
