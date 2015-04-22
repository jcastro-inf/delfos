package delfos.group.grs.itemweighted;

import delfos.common.Chronometer;
import delfos.common.aggregationoperators.Mean;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.SingleRecommenderSystemModel;
import delfos.group.grs.itemweighted.knn.memory.KnnMemoryBasedNWR_itemWeighted;
import delfos.group.grs.itemweighted.measures.StandardDeviationWeights;
import delfos.group.grs.itemweighted.measures.Tweak2Weights;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.output.RecommendationsOutputFileXML;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.io.File;
import java.util.Collection;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class AggregationOfIndividualRatings_itemWeightedTest extends DelfosTest {

    private static final String TEST_DIRECTORY = getTemporalDirectoryForTest(AggregationOfIndividualRatings_itemWeightedTest.class).getPath() + File.separator;

    public AggregationOfIndividualRatings_itemWeightedTest() {
    }

    private final DatasetLoader<? extends Rating> datasetLoader = new ConfiguredDatasetLoader("ml-100k");

    @Test
    public void testGRSItemVarianceAsItemWeights_ratingCompletion() throws Exception {
        Chronometer chronometer = new Chronometer();
        KnnMemoryBasedNWR_itemWeighted knnMemory_itemWeighted = getCoreRS();

        AggregationOfIndividualRatings_itemWeighted grs = new AggregationOfIndividualRatings_itemWeighted(knnMemory_itemWeighted, new Mean(), new StandardDeviationWeights(), 0.0);
        grs.addBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 1000));

        GroupOfUsers groupOfUsers = new GroupOfUsers(1, 2, 3, 4, 5);
        SingleRecommenderSystemModel recommendationModel = grs.build(datasetLoader);

        GroupModelWithExplanation groupModel = grs.buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);

        Collection<Recommendation> recommendOnly = grs.recommendOnly(datasetLoader, recommendationModel, groupModel, groupOfUsers, datasetLoader.getRatingsDataset().allRatedItems());

        long timeTaken = chronometer.getTotalElapsed();
        writeRecommendationsInStandardOutput(grs, new GroupRecommendations(groupOfUsers, recommendOnly, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken)));
    }

    @Test
    public void testGRSItemVarianceTweak2AsItemWeights_ratingCompletion() throws Exception {
        Chronometer chronometer = new Chronometer();

        KnnMemoryBasedNWR_itemWeighted knnMemory_itemWeighted = getCoreRS();

        AggregationOfIndividualRatings_itemWeighted grs = new AggregationOfIndividualRatings_itemWeighted(knnMemory_itemWeighted, new Mean(), new Tweak2Weights(), 0.0);
        grs.addBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 1000));

        GroupOfUsers groupOfUsers = new GroupOfUsers(1, 2, 3, 4, 5);
        SingleRecommenderSystemModel recommendationModel = grs.build(datasetLoader);
        GroupModelWithExplanation groupModel = grs.buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);

        Collection<Recommendation> recommendOnly = grs.recommendOnly(datasetLoader, recommendationModel, groupModel, groupOfUsers, datasetLoader.getRatingsDataset().allRatedItems());

        long timeTaken = chronometer.getTotalElapsed();
        writeRecommendationsInStandardOutput(grs, new GroupRecommendations(groupOfUsers, recommendOnly, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken)));
    }

    private KnnMemoryBasedNWR_itemWeighted getCoreRS() {
        KnnMemoryBasedNWR_itemWeighted knnMemory_itemWeighted = new KnnMemoryBasedNWR_itemWeighted(new PearsonCorrelationCoefficient(), 20, 30, new WeightedSum());
        knnMemory_itemWeighted.setAlias("knnMemory_itemWeighted");
        return knnMemory_itemWeighted;
    }

    @Test
    public void testGRSItemVarianceTweak2AsItemWeights() throws Exception {
        Chronometer chronometer = new Chronometer();
        KnnMemoryBasedNWR_itemWeighted knnMemory_itemWeighted = getCoreRS();
        AggregationOfIndividualRatings_itemWeighted grs = new AggregationOfIndividualRatings_itemWeighted(knnMemory_itemWeighted, new Mean(), new Tweak2Weights(), null);
        GroupOfUsers groupOfUsers = new GroupOfUsers(1, 2, 3, 4, 5);
        SingleRecommenderSystemModel recommendationModel = grs.build(datasetLoader);
        GroupModelWithExplanation groupModel = grs.buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);
        Collection<Recommendation> recommendOnly = grs.recommendOnly(datasetLoader, recommendationModel, groupModel, groupOfUsers, datasetLoader.getRatingsDataset().allRatedItems());

        long timeTaken = chronometer.getTotalElapsed();
        GroupRecommendations recommendations = new GroupRecommendations(groupOfUsers, recommendOnly, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken));

        writeRecommendationsInStandardOutput(grs, recommendations);
    }

    private void writeRecommendationsInStandardOutput(GroupRecommenderSystem grs, GroupRecommendations recommendations) {
        RecommendationsOutputMethod recommendationsOutput = new RecommendationsOutputStandardRaw();
        recommendationsOutput.writeRecommendations(recommendations);

        RecommendationsOutputMethod fileOutput = new RecommendationsOutputFileXML(TEST_DIRECTORY + grs.getAlias() + "_recommendations.xml");
        fileOutput.writeRecommendations(recommendations);
    }

    @Test
    public void testGRSItemVarianceAsItemWeights() throws Exception {
        Chronometer chronometer = new Chronometer();
        KnnMemoryBasedNWR_itemWeighted knnMemory_itemWeighted = getCoreRS();
        AggregationOfIndividualRatings_itemWeighted grs = new AggregationOfIndividualRatings_itemWeighted(knnMemory_itemWeighted, new Mean(), new StandardDeviationWeights(), 0.0);
        GroupOfUsers groupOfUsers = new GroupOfUsers(1, 2, 3, 4, 5);
        SingleRecommenderSystemModel recommendationModel = grs.build(datasetLoader);
        GroupModelWithExplanation groupModel = grs.buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);
        Collection<Recommendation> recommendOnly = grs.recommendOnly(datasetLoader, recommendationModel, groupModel, groupOfUsers, datasetLoader.getRatingsDataset().allRatedItems());

        long timeTaken = chronometer.getTotalElapsed();
        writeRecommendationsInStandardOutput(grs, new GroupRecommendations(groupOfUsers, recommendOnly, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken)));
    }
}
