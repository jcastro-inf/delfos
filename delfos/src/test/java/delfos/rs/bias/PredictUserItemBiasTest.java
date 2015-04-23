package delfos.rs.bias;

import delfos.common.datastructures.histograms.HistogramNumbersSmart;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.generated.recommender.RecommenderBasedDataset;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.Coverage;
import delfos.results.evaluationmeasures.ratingprediction.MAE;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.SingleUserRecommendations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class PredictUserItemBiasTest {

    public PredictUserItemBiasTest() {
    }

    @Test
    public void testRecommendOnly() throws Exception {
        System.out.println("testRecommendOnly");

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        User user = new User(45);
        Set<Integer> candidateItems = datasetLoader.getRatingsDataset().allRatedItems();
        PredictUserItemBias bias = new PredictUserItemBias();

        Object model = bias.build(datasetLoader);

        SingleUserRecommendations singleUserRecommendations = new SingleUserRecommendations(user, bias.recommendOnly(datasetLoader, model, user.getId(), candidateItems));

        RatingsDataset rd = new RecommenderBasedDataset(datasetLoader);

        MeanIterative userMAE = new MAE().getUserResult(
                singleUserRecommendations,
                datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId())
        );

        System.out.println("User " + user.getTargetId() + " mae is '" + userMAE.getMean() + "'");

    }

    @Test
    public void testCoverageIsOne() throws Exception {
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        PredictUserItemBias bias = new PredictUserItemBias();
        Object model = bias.build(datasetLoader);

        HistogramNumbersSmart histogramMAE = new HistogramNumbersSmart(0, datasetLoader.getRatingsDataset().getRatingsDomain().width().doubleValue(), 0.05);
        HistogramNumbersSmart histogramCoverage = new HistogramNumbersSmart(0, 1, 0.05);

        List<SingleUserRecommendations> allRecommendations = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers().size());
        for (int idUser : datasetLoader.getRatingsDataset().allUsers()) {
            User user = new User(idUser);
            Collection<Recommendation> recommendations = bias.recommendOnly(datasetLoader, model, idUser, datasetLoader.getRatingsDataset().getUserRated(idUser));
            final SingleUserRecommendations singleUserRecommendations = new SingleUserRecommendations(new User(idUser), recommendations);
            allRecommendations.add(singleUserRecommendations);
            final Map<Integer, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

            MeanIterative userMAE = new MAE().getUserResult(singleUserRecommendations, userRatingsRated);
            MeanIterative userCoverage = new Coverage().getUserResult(singleUserRecommendations, userRatingsRated);

            if (userCoverage.getMean() != 1) {
                fail("No user should get a coverage lower than 1.");
            }

            System.out.println("User " + user.getTargetId() + " coverage '" + userCoverage.getMean() + "' mae '" + userMAE.getMean() + "'");

            histogramMAE.addValue(userMAE.getMean());
            histogramCoverage.addValue(userCoverage.getMean());
        }

        System.out.println("==============================================================");
        System.out.println("==== mae histogram for ml-100k and bias recommender ==========");
        System.out.println("==============================================================");

        histogramMAE.printHistogram(System.out);
        System.out.println("==============================================================");
        System.out.println("==== coverage histogram for ml-100k and bias recommender ==========");
        System.out.println("==============================================================");

        histogramCoverage.printHistogram(System.out);

        RecommendationResults recommendationResults = new RecommendationResults(allRecommendations);

        MeasureResult measureResult = new Coverage().getMeasureResult(recommendationResults, datasetLoader.getRatingsDataset(), new RelevanceCriteria(4));
        float coverage = measureResult.getValue();

        Assert.assertEquals(1, coverage, Double.MIN_VALUE);
    }

}
