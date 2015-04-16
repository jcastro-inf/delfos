package delfos.results.evaluationmeasures.ratingprediction;

import delfos.results.evaluationmeasures.ratingprediction.RMSE;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.RecommendationResults;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.RatingsDatasetMock;
import delfos.rs.recommendation.Recommendation;

/**
 * Implementa tests para comprobar que el RMSE se está calculando correctamente.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 19-febrero-2014
 */
public class RMSETest {

    public RMSETest() {
    }

    /**
     * Test of getMeasureResult method, of class RMSE.
     */
    @Test
    public void test_noRecommendations_RMSEequalsNaN() {
        System.out.println("test_noRecommendations_RMSEequalsNaN");

        //Phase 1: Preparation
        int idUser = 1;
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        recommendationResults.add(idUser, new ArrayList<>());
        RMSE instance = new RMSE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = Float.NaN;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }

    @Test
    public void test_allMovedOne_RMSEequals1() {
        System.out.println("test_allMovedOne_RMSEequals1");

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        int i = 0;
        for (int idUser : testDataset.allUsers()) {
            try {
                ArrayList<Recommendation> recommendations = new ArrayList<Recommendation>();
                for (Map.Entry<Integer, ? extends Rating> entry : testDataset.getUserRatingsRated(idUser).entrySet()) {
                    final int idItem = entry.getKey();
                    final Rating rating = entry.getValue();

                    final double prediction;
                    if (i % 2 == 0) {
                        prediction = rating.ratingValue.doubleValue() + 1;
                    } else {
                        prediction = rating.ratingValue.doubleValue() - 1;
                    }
                    recommendations.add(new Recommendation(idItem, prediction));
                    i++;
                }
                Collections.sort(recommendations);
                recommendationResults.add(idUser, recommendations);
            } catch (UserNotFound ex) {
                //Never happens if the dataset is properly implemented
            }
        }
        RMSE instance = new RMSE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = 1;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }

    @Test
    public void test_allMovedTwo_RMSEequals2() {
        System.out.println("test_allMovedTwo_RMSEequals1dot41");

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        int i = 0;
        for (int idUser : testDataset.allUsers()) {
            try {
                ArrayList<Recommendation> recommendations = new ArrayList<Recommendation>();
                for (Map.Entry<Integer, ? extends Rating> entry : testDataset.getUserRatingsRated(idUser).entrySet()) {
                    final int idItem = entry.getKey();
                    final Rating rating = entry.getValue();

                    final double prediction;
                    if (i % 2 == 0) {
                        prediction = rating.ratingValue.doubleValue() + 2;
                    } else {
                        prediction = rating.ratingValue.doubleValue() - 2;
                    }
                    recommendations.add(new Recommendation(idItem, prediction));
                    i++;
                }
                Collections.sort(recommendations);
                recommendationResults.add(idUser, recommendations);
            } catch (UserNotFound ex) {
                //Never happens if the dataset is properly implemented
            }
        }
        RMSE instance = new RMSE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = 2;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }

    @Test
    public void test_perfectPrediction_RMSEequals0() {
        System.out.println("test_perfectPrediction_RMSEequals0");

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        for (int idUser : testDataset.allUsers()) {
            try {
                ArrayList<Recommendation> recommendations = new ArrayList<Recommendation>();
                for (Map.Entry<Integer, ? extends Rating> entry : testDataset.getUserRatingsRated(idUser).entrySet()) {
                    final int idItem = entry.getKey();
                    final Rating rating = entry.getValue();

                    final double prediction = rating.ratingValue.doubleValue();
                    recommendations.add(new Recommendation(idItem, prediction));
                }
                Collections.sort(recommendations);
                recommendationResults.add(idUser, recommendations);
            } catch (UserNotFound ex) {
                //Never happens if the dataset is properly implemented
            }
        }
        RMSE instance = new RMSE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = 0;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }

    @Test
    public void test_perfectPredictionButOneMovedBy3_RMSEequals1dot732() {
        System.out.println("test_perfectPrediction_RMSEequals0");

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();

        int idUser = 1;

        ArrayList<Recommendation> recommendations = new ArrayList<Recommendation>();
        recommendations.add(new Recommendation(10, 4));
        recommendations.add(new Recommendation(12, 5));
        recommendations.add(new Recommendation(13, 2));

        Collections.sort(recommendations);
        recommendationResults.add(idUser, recommendations);

        RMSE instance = new RMSE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = 1.732f;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }
}
