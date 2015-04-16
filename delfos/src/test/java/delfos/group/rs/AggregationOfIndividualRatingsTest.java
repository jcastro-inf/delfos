package delfos.group.rs;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.common.Global;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommenderSystemModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.similaritymeasures.CosineCoefficient;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 23-Jan-2013
 */
public class AggregationOfIndividualRatingsTest {

    private static FilePersistence filePersistence;
    private static CSVfileDatasetLoader datasetLoader;

    public AggregationOfIndividualRatingsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        datasetLoader = new CSVfileDatasetLoader("datasets" + File.separator + "SSII - ratings9.csv", "datasets" + File.separator + "SSII - peliculas.csv");
        try {
            datasetLoader.getRatingsDataset();
        } catch (CannotLoadRatingsDataset ex) {
            Global.showError(ex);
        }
        filePersistence = new FilePersistence("model", "dat");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testWholeProccess() throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {
        Global.setVerbose();

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        final ContentDataset contentDataset = datasetLoader.getContentDataset();

        KnnMemoryBasedNWR traditionalRecommenderSystem = new KnnMemoryBasedNWR(new CosineCoefficient(), 30, null, false, 1, 20, new WeightedSum());

        AggregationOfIndividualRatings grs = new AggregationOfIndividualRatings(traditionalRecommenderSystem, new Mean());

        grs.addBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 5000));
        SingleRecommenderSystemModel recommenderSystemModel = grs.build(datasetLoader);

        Set<Integer> notRated = new TreeSet<>(ratingsDataset.allRatedItems());

        notRated.removeAll(ratingsDataset.getUserRated(15743));
        notRated.removeAll(ratingsDataset.getUserRated(24357));
        notRated.removeAll(ratingsDataset.getUserRated(162779));

        GroupOfUsers group = new GroupOfUsers(15743, 24357, 162779);

        GroupModelWithExplanation<GroupModelPseudoUser, ? extends Object> groupModel = grs.buildGroupModel(datasetLoader, recommenderSystemModel, group);
        List<Recommendation> recommendOnly = grs.recommendOnly(datasetLoader, recommenderSystemModel, groupModel, group, notRated);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();
        output.writeRecommendations(new GroupRecommendations(group, recommendOnly, RecommendationComputationDetails.EMPTY_DETAILS));

        {
            // 4697 -> 4.300307
            int idItem = 4697;
            double prediction = 4.300307;
            List<Recommendation> predictionList = grs.recommendOnly(datasetLoader, recommenderSystemModel, groupModel, group, idItem);
            Assert.assertEquals(1, predictionList.size());
            Assert.assertEquals(idItem, predictionList.get(0).getIdItem());
            Assert.assertEquals(prediction, predictionList.get(0).getPreference().doubleValue(), 0.0001);
        }

        {
            // 10082 -> 2.50053
            int idItem = 10082;
            double prediction = 2.50053;
            List<Recommendation> predictionList = grs.recommendOnly(datasetLoader, recommenderSystemModel, groupModel, group, idItem);
            Assert.assertEquals(1, predictionList.size());
            Assert.assertEquals(idItem, predictionList.get(0).getIdItem());
            Assert.assertEquals(prediction, predictionList.get(0).getPreference().doubleValue(), 0.0001);
        }
    }
}
