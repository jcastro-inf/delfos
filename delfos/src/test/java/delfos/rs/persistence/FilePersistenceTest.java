package delfos.rs.persistence;

import delfos.common.Chronometer;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract_withTrust;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.factories.RecommenderSystemsFactory;
import delfos.io.csv.dataset.DatasetToCSV;
import delfos.main.managers.recommendation.singleuser.SingleUserRecommendation;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.Recommender_DatasetProperties;
import delfos.rs.output.RecommendationsOutputFileXML;
import java.io.File;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

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
public class FilePersistenceTest extends DelfosTest {

    public static final String TEST_DIRECTORY = getTemporalDirectoryForTest(FilePersistenceTest.class).getPath() + File.separator;
    public static final String RECOMMENDATIONS_DIRECTORY = TEST_DIRECTORY + "recommendationsFileOutput" + File.separator;
    public static final String RATINGS_DATASET_FILE = TEST_DIRECTORY + "dataset" + File.separator + "test_rating_dataset.csv";
    public static final String CONTENT_DATASET_FILE = TEST_DIRECTORY + "dataset" + File.separator + "test_content_dataset.csv";
    public static final String USERS_DATASET_FILE = TEST_DIRECTORY + "dataset" + File.separator + "test_users_dataset.csv";
    protected final static DatasetLoader<? extends Rating> datasetLoader = new CSVfileDatasetLoader(
            RATINGS_DATASET_FILE,
            CONTENT_DATASET_FILE,
            USERS_DATASET_FILE);

    public FilePersistenceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        FileUtilities.cleanDirectory(new File(TEST_DIRECTORY));

        CompleteDatasetLoaderAbstract_withTrust<Rating> mockDatasetLoader = new MockDatasetLoader();

        DatasetToCSV datasetToCSV = new DatasetToCSV(
                new File(RATINGS_DATASET_FILE),
                new File(CONTENT_DATASET_FILE),
                new File(USERS_DATASET_FILE)
        );
        datasetToCSV.saveRatingsDataset(mockDatasetLoader.getRatingsDataset());
        datasetToCSV.saveContentDataset(mockDatasetLoader.getContentDataset());
        datasetToCSV.saveUsersDataset(mockDatasetLoader.getUsersDataset());

        datasetLoader.getRatingsDataset();
        ((ContentDatasetLoader) datasetLoader).getContentDataset();
        ((UsersDatasetLoader) datasetLoader).getUsersDataset();

    }

    @Test
    public void testAllRecommenderSystemsFilePersistence() throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        final List<RecommenderSystem> recommenderSystems = RecommenderSystemsFactory.getInstance().getAllClasses(RecommenderSystem.class);

        for (RecommenderSystem recommenderSystem : recommenderSystems) {
            if (recommenderSystem instanceof Recommender_DatasetProperties) {
                continue;
            }

            String configFile = TEST_DIRECTORY + recommenderSystem.getName() + "_config.xml";

            RecommenderSystemConfigurationFileParser.saveConfigFile(
                    configFile,
                    recommenderSystem,
                    datasetLoader,
                    new FilePersistence(
                            recommenderSystem.getName(),
                            "dat",
                            new File(TEST_DIRECTORY)),
                    new OnlyNewItems(),
                    getRecommendationOutputFileXML(recommenderSystem));

            Chronometer chronometer = new Chronometer();
            SingleUserRecommendation.buildRecommendationModel(configFile);
            Global.showln("Built model of '" + recommenderSystem + "' in " + chronometer.printTotalElapsed());

            RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
            chronometer.reset();
            ratingsDataset.allUsers().stream().forEach((idUser) -> {
                SingleUserRecommendation.recommendToUser(configFile, idUser);
            });
            Global.showln("Recommended with '" + recommenderSystem + "' to " + datasetLoader.getRatingsDataset().allUsers().size() + " users in " + chronometer.printTotalElapsed());

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
