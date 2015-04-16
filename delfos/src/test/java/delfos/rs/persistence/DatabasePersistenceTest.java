package delfos.rs.persistence;

import delfos.rs.persistence.FilePersistence;
import java.io.File;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.common.Chronometer;
import delfos.common.FileUtilities;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.constants.DelfosTest;
import delfos.constants.TestConstants;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader_Default;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.factories.RecommenderSystemsFactory;
import delfos.io.database.mysql.dataset.ContentDatasetToMySQL;
import delfos.io.database.mysql.dataset.RatingDatasetToMySQL;
import delfos.io.database.mysql.dataset.UsersDatasetToMySQL;
import delfos.main.managers.recommendation.singleuser.BuildRecommendationModel;
import delfos.main.managers.recommendation.singleuser.Recommend;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.Recommender_DatasetProperties;
import delfos.rs.output.RecommendationsOutputFileXML;

/**
 * En esta clase se implementa un test que comprueba el correcto funcionamiento
 * de los sistemas de recomendación que implementan la interfaz
 * {@link RecommenderSystemWithFilePersitence}. Para ello, crea unos datasets
 * sencillos y comprueba que no ocurren errores en la construcción del modelo o
 * en la recomendación.
 *
 * <p>
 * <p>
 * NOTA: Sólo comprueba que funciona con la configuración por defecto.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 04-Mar-2013
 */
public class DatabasePersistenceTest extends DelfosTest {

    public static final String TEST_DIRECTORY = TestConstants.TEST_DATA_DIRECTORY + DatabasePersistenceTest.class.getSimpleName() + File.separator;
    public static final String RECOMMENDATIONS_DIRECTORY = TEST_DIRECTORY + "recommendationsFileOutput" + File.separator;
    public static final String user = TestConstants.databaseTestUserName;
    public static final String pass = TestConstants.databaseTestPassword;
    public static final String databaseName = TestConstants.databaseTestDatabaseName;
    public static final int port = TestConstants.databaseTestPort;
    public static final String hostName = TestConstants.databaseTestHost;
    public static final String TABLE_NAME_PREFIX = "database_persistence_test_";

    protected final static DatasetLoader<? extends Rating> datasetLoader;

    static {
        datasetLoader = new MySQLDatabaseDatasetLoader_Default(user, pass, databaseName, hostName, port, TABLE_NAME_PREFIX);
    }

    public DatabasePersistenceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FileUtilities.cleanDirectory(new File(TEST_DIRECTORY));

        //Se crean los  dataset en la base de datos.
        new File(RECOMMENDATIONS_DIRECTORY).mkdirs();

        MySQLConnection mySQLConnection = new MySQLConnection(user, pass, databaseName, hostName, port, TABLE_NAME_PREFIX);

        MockDatasetLoader mockDatasetLoader = new MockDatasetLoader();

        RatingDatasetToMySQL ratingDatasetToMySQL = new RatingDatasetToMySQL(mySQLConnection);
        ratingDatasetToMySQL.writeDataset(mockDatasetLoader.getRatingsDataset());

        ContentDatasetToMySQL contentDatasetToMySQL = new ContentDatasetToMySQL(mySQLConnection);
        contentDatasetToMySQL.writeDataset(mockDatasetLoader.getContentDataset());

        UsersDatasetToMySQL usersDatasetToMySQL = new UsersDatasetToMySQL(mySQLConnection);
        usersDatasetToMySQL.writeDataset(mockDatasetLoader.getUsersDataset());
    }

    @Test
    public void testAllRecommenderSystemsDatabasePersistence() throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        final List<RecommenderSystem> recommenderSystems = RecommenderSystemsFactory.getInstance().getAllClasses(RecommenderSystem.class);

        for (RecommenderSystem recommenderSystem : recommenderSystems) {
            if (recommenderSystem instanceof Recommender_DatasetProperties) {
                continue;
            }

            String configFile = TEST_DIRECTORY + recommenderSystem.getName() + "_config.xml";

            RecommendationsOutputFileXML recommendationsOutput = getRecommendationOutputFileXML(recommenderSystem);
            RecommenderSystemConfigurationFileParser.saveConfigFile(
                    configFile,
                    recommenderSystem,
                    datasetLoader,
                    new FilePersistence(
                            recommenderSystem.getName(),
                            "dat",
                            new File(TEST_DIRECTORY)),
                    new OnlyNewItems(),
                    recommendationsOutput);

            Chronometer c = new Chronometer();
            BuildRecommendationModel.buildRecommendationModel(configFile);
            System.out.println("Built model of '" + recommenderSystem + "' in " + c.printTotalElapsed());

            c.reset();
            datasetLoader.getRatingsDataset()
                    .allUsers()
                    .stream()
                    .forEach((idUser) -> {
                        Recommend.recommendToUser(configFile, idUser);
                    });

            System.out.print("Recommended with '" + recommenderSystem + "' to " + datasetLoader.getRatingsDataset().allUsers().size() + " in " + c.printTotalElapsed() + "\n");
        }
    }

    protected static RecommendationsOutputFileXML getRecommendationOutputFileXML(RecommenderSystem recommenderSystem) {
        String ret = RECOMMENDATIONS_DIRECTORY + recommenderSystem.getName() + "_recommendations";
        return new RecommendationsOutputFileXML(ret);
    }

    protected static File getRecommendationFile(RecommenderSystem recommenderSystem, User user) {
        RecommendationsOutputFileXML outputFileXML = getRecommendationOutputFileXML(recommenderSystem);
        File thisUserRecommendationsFile = outputFileXML.getCompleteFile(user.getTargetId());
        return thisUserRecommendationsFile;
    }
}
