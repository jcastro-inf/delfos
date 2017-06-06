package delfos.rs.bias;

import delfos.common.Global;
import delfos.common.datastructures.histograms.HistogramNumbersSmart;
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
import delfos.rs.recommendation.RecommendationsToUser;
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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PredictUserItemBiasTest {

    public PredictUserItemBiasTest() {
    }

    @Test
    public void testRecommendOnly() throws Exception {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        User user = new User(45);
        Set<Long> candidateItems = datasetLoader.getRatingsDataset().allRatedItems();
        PredictUserItemBias bias = new PredictUserItemBias();

        Object model = bias.buildRecommendationModel(datasetLoader);

        RecommendationsToUser recommendationsToUser = new RecommendationsToUser(user, bias.recommendToUser(datasetLoader, model, user.getId(), candidateItems));

        RatingsDataset rd = new RecommenderBasedDataset(datasetLoader);

        MeasureResult userMAE = new MAE().getUserResult(
                recommendationsToUser,
                datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId())
        );

        Global.showln("User " + user.getTargetId() + " mae is '" + userMAE.getValue() + "'");

    }

    @Test
    public void testCoverageIsOne() throws Exception {
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        PredictUserItemBias bias = new PredictUserItemBias();
        Object model = bias.buildRecommendationModel(datasetLoader);

        HistogramNumbersSmart histogramMAE = new HistogramNumbersSmart(0, datasetLoader.getRatingsDataset().getRatingsDomain().width().doubleValue(), 0.05);
        HistogramNumbersSmart histogramCoverage = new HistogramNumbersSmart(0, 1, 0.05);

        List<RecommendationsToUser> allRecommendations = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers().size());
        for (long idUser : datasetLoader.getRatingsDataset().allUsers()) {
            User user = new User(idUser);
            Collection<Recommendation> recommendations = bias.recommendToUser(datasetLoader, model, idUser, datasetLoader.getRatingsDataset().getUserRated(idUser));
            final RecommendationsToUser singleUserRecommendations = new RecommendationsToUser(new User(idUser), recommendations);
            allRecommendations.add(singleUserRecommendations);
            final Map<Long, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

            MeasureResult userMAE = new MAE().getUserResult(singleUserRecommendations, userRatingsRated);
            MeasureResult userCoverage = new Coverage().getUserResult(singleUserRecommendations, userRatingsRated);

            if (userCoverage.getValue() != 1) {
                fail("No user should get a coverage lower than 1.");
            }

            Global.showln("User " + user.getTargetId() + " coverage '" + userCoverage.getValue() + "' mae '" + userMAE.getValue() + "'");

            histogramMAE.addValue(userMAE.getValue());
            histogramCoverage.addValue(userCoverage.getValue());
        }

        Global.showln("==============================================================");
        Global.showln("==== mae histogram for ml-100k and bias recommender ==========");
        Global.showln("==============================================================");

        histogramMAE.printHistogram(System.out);
        Global.showln("==============================================================");
        Global.showln("==== coverage histogram for ml-100k and bias recommender ==========");
        Global.showln("==============================================================");

        histogramCoverage.printHistogram(System.out);

        RecommendationResults recommendationResults = new RecommendationResults(allRecommendations);

        MeasureResult measureResult = new Coverage().getMeasureResult(recommendationResults, datasetLoader.getRatingsDataset(), new RelevanceCriteria(4));
        double coverage = measureResult.getValue();

        Assert.assertEquals(1, coverage, Double.MIN_VALUE);
    }

}
