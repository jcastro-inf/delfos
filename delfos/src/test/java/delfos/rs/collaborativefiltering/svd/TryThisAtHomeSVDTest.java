package delfos.rs.collaborativefiltering.svd;

import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.main.Main;
import delfos.main.managers.recommendation.singleuser.SingleUserRecommendation;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.DatabasePersistenceTest;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 22-Mar-2013
 */
public class TryThisAtHomeSVDTest extends DelfosTest {

    private static MySQLConnection mySQLConnection;

    private static final Integer[] users = {727, 38, 466, 232, 927, 915, 764, 382, 649, 645};

    public TryThisAtHomeSVDTest() {
    }
    public static final String TEST_DIRECTORY = getTemporalDirectoryForTest(TryThisAtHomeSVDTest.class).getPath() + File.separator;

    public static final String TABLE_NAME_PREFIX = "trythisathome_test_";

    @BeforeClass
    public static void setUpClass()
            throws Exception {

        FileUtilities.cleanDirectory(new File(TEST_DIRECTORY));

        mySQLConnection = new MySQLConnection(
                DatabasePersistenceTest.user,
                DatabasePersistenceTest.pass,
                DatabasePersistenceTest.databaseName,
                DatabasePersistenceTest.hostName,
                DatabasePersistenceTest.port,
                TryThisAtHomeSVDTest.TABLE_NAME_PREFIX
        );
    }

    @Test
    public void testRecommendation() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, ItemNotFound {

        final DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        final TryThisAtHomeSVD recommenderSystem = new TryThisAtHomeSVD(5, 10);
        final TryThisAtHomeSVDModel model = recommenderSystem.buildRecommendationModel(datasetLoader);
        final RecommendationCandidatesSelector candidates = new OnlyNewItems();

        for (int idUser : users) {
            Set<Integer> candidateItems = candidates.candidateItems(datasetLoader, new User(idUser));
            Collection<Recommendation> recommendOnly = recommenderSystem.recommendToUser(datasetLoader, model, idUser, candidateItems);
        }
    }

    @Test
    public void testFilePersistenceRecommendation() throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        final DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        TryThisAtHomeSVD recommenderSystem = new TryThisAtHomeSVD();
        String configFile = TEST_DIRECTORY + recommenderSystem.getName() + "_config.xml";
        RecommenderSystemConfigurationFileParser.saveConfigFile(
                configFile,
                recommenderSystem,
                datasetLoader,
                new FilePersistence(recommenderSystem.getName(), "dat", new File(TEST_DIRECTORY)), new OnlyNewItems(), new RecommendationsOutputStandardRaw(5));

        SingleUserRecommendation.buildRecommendationModel(configFile);
        Global.showln("Built recommendation model with '" + recommenderSystem);

        Arrays.asList(users)
                .parallelStream()
                .forEach((idUser) -> {
                    SingleUserRecommendation.recommendToUser(configFile, idUser);
                });

        Global.showln("Recommended with '" + recommenderSystem + "' to " + datasetLoader.getRatingsDataset().allUsers().size() + " users");
    }

    @Test
    public void testDatabasePersistenceRecommendation() throws ClassNotFoundException, SQLException, CannotLoadRatingsDataset, CannotLoadContentDataset {

        final DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        TryThisAtHomeSVD recommenderSystem = new TryThisAtHomeSVD();
        String configFile = TEST_DIRECTORY + recommenderSystem.getName() + "_config.xml";
        RecommenderSystemConfigurationFileParser.saveConfigFile(
                configFile,
                recommenderSystem,
                datasetLoader,
                new DatabasePersistence(mySQLConnection),
                new OnlyNewItems(),
                new RecommendationsOutputStandardRaw(5));

        //Construcción del modelo.
        String[] buildArgs = {"--single-user", "--b", "-rs-config", configFile};
        Main.mainWithExceptions(buildArgs);
        Global.showln("Built model of '" + recommenderSystem);

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        Arrays.asList(users)
                .parallelStream()
                .map((idUser) -> {
                    String[] args = {"--single-user", "--r", "-u", idUser.toString(), "-rs-config", configFile};
                    return args;
                })
                .forEach((recommendationArgs) -> {
                    Main.mainWithExceptions(recommendationArgs);
                });

        Global.showln("Recommended with '" + recommenderSystem + "' to " + datasetLoader.getRatingsDataset().allUsers().size() + " users");
    }

    @Test
    public void testLearning() throws Exception {

        MockDatasetLoader datasetLoader = new MockDatasetLoader();

        TryThisAtHomeSVD tryThisAtHomeSVD = new TryThisAtHomeSVD(1, 400);
        TryThisAtHomeSVDModel tryThisAtHomeSVDModel = tryThisAtHomeSVD.buildRecommendationModel(datasetLoader);

        Set<Integer> candidateItems = new TreeSet<>();
        candidateItems.add(2);
        candidateItems.add(4);

        Global.showln("Features learned USERS");

        tryThisAtHomeSVDModel.getUsersIndex().keySet()
                .parallelStream()
                .forEach((idUser) -> {
                    Global.showln("User " + idUser + "--> " + tryThisAtHomeSVDModel.getUserFeatures(idUser));
                });

        Global.showln("\nFeatures learned ITEMS");
        tryThisAtHomeSVDModel.getItemsIndex().keySet()
                .parallelStream()
                .forEach((idItem) -> {
                    Global.showln("Item " + idItem + "--> " + tryThisAtHomeSVDModel.getItemFeatures(idItem));
                });

        Collection<Recommendation> recommendations = tryThisAtHomeSVD.recommendToUser(datasetLoader, tryThisAtHomeSVDModel, 3, candidateItems);

        List<Recommendation> sortedRecommendations = new ArrayList<>(recommendations);
        Collections.sort(sortedRecommendations);

        assert sortedRecommendations.get(0).getIdItem() == 4;
        Assert.assertEquals(4, sortedRecommendations.get(0).getPreference().doubleValue(), 0.2);
        assert sortedRecommendations.get(1).getIdItem() == 2;
        Assert.assertEquals(2, sortedRecommendations.get(1).getPreference().doubleValue(), 0.2);
    }
}
