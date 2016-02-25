package delfos.results.evaluationmeasures.ratingprediction;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.RatingsDatasetMock;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Implementa tests para comprobar que el mae se está calculando correctamente.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 19-febrero-2014
 */
public class MAETest {

    public MAETest() {
    }

    /**
     * Test of getMeasureResult method, of class MAE.
     */
    @Test
    public void test_noRecommendations_MAEequalsNaN() {

        //Phase 1: Preparation
        int idUser = 1;
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);

        RecommendationResults recommendationResults = new RecommendationResults();
        recommendationResults.add(idUser, new ArrayList<>());

        MAE instance = new MAE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = Float.NaN;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }

    @Test
    public void test_allMovedOne_MAEequals1() throws UserNotFound {

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        int i = 0;
        for (int idUser : testDataset.allUsers()) {
            ArrayList<Recommendation> recommendations = new ArrayList<>();
            for (Map.Entry<Integer, ? extends Rating> entry : testDataset.getUserRatingsRated(idUser).entrySet()) {
                final int idItem = entry.getKey();
                final Rating rating = entry.getValue();

                double prediction;
                if (i % 2 == 0) {
                    prediction = rating.getRatingValue().doubleValue() + 1;
                    if (prediction > testDataset.getRatingsDomain().max().doubleValue()) {
                        prediction = rating.getRatingValue().doubleValue() - 1;
                    }
                } else {
                    prediction = rating.getRatingValue().doubleValue() - 1;
                    if (prediction < testDataset.getRatingsDomain().min().doubleValue()) {
                        prediction = rating.getRatingValue().doubleValue() + 1;
                    }
                }
                recommendations.add(new Recommendation(idItem, prediction));
                i++;
            }
            Collections.sort(recommendations);
            recommendationResults.add(idUser, recommendations);

        }
        MAE instance = new MAE();

        //Phase 2: Execution
        MeasureResult MAEResult = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);
        double maeValue = MAEResult.getValue();

        //Phase 3: Result checking
        double expResult = 1;
        double delta = 0.001f;
        assertEquals(expResult, maeValue, delta);
    }

    @Test
    public void test_perfectPrediction_MAEequals0() throws UserNotFound {

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        for (int idUser : testDataset.allUsers()) {
            ArrayList<Recommendation> recommendations = new ArrayList<>();
            for (Map.Entry<Integer, ? extends Rating> entry : testDataset.getUserRatingsRated(idUser).entrySet()) {
                final int idItem = entry.getKey();
                final Rating rating = entry.getValue();

                final double prediction = rating.getRatingValue().doubleValue();
                recommendations.add(new Recommendation(idItem, prediction));
            }
            Collections.sort(recommendations);
            recommendationResults.add(idUser, recommendations);
        }
        MAE instance = new MAE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        double expResult = 0;
        double delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }
}
