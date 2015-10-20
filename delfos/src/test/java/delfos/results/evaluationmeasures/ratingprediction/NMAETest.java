package delfos.results.evaluationmeasures.ratingprediction;

import delfos.results.evaluationmeasures.ratingprediction.NMAE;
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
 * Implementa tests para {@link NMAE}.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 19-febrero-2014
 */
public class NMAETest {

    public NMAETest() {
    }

    /**
     * Test of getMeasureResult method, of class NMAE.
     */
    @Test
    public void test_noRecommendations_NMAEequalsNaN() {
        System.out.println("test_noRecommendations_NMAEequalsNaN");

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        testDataset.allUsers().stream().forEach((idUser) -> {
            recommendationResults.add(idUser, new ArrayList<>());
        });
        NMAE instance = new NMAE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = Float.NaN;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }

    @Test
    public void test_allMovedOne_NMAEEquals0point25() {
        System.out.println("test_allMovedOne_NMAEEquals1");

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        int i = 0;
        for (int idUser : testDataset.allUsers()) {
            try {
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
            } catch (UserNotFound ex) {
                //Never happens if the dataset is properly implemented
            }
        }
        NMAE instance = new NMAE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = 0.25f;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }

    @Test
    public void test_allMovedTwo_NMAEEquals0point5() {
        System.out.println("test_allMovedTwo_NMAEEquals2");

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        int i = 0;
        for (int idUser : testDataset.allUsers()) {
            try {
                ArrayList<Recommendation> recommendations = new ArrayList<>();
                for (Map.Entry<Integer, ? extends Rating> entry : testDataset.getUserRatingsRated(idUser).entrySet()) {
                    final int idItem = entry.getKey();
                    final Rating rating = entry.getValue();

                    double prediction;
                    if (i % 2 == 0) {
                        prediction = rating.getRatingValue().doubleValue() + 2;
                        if (prediction > testDataset.getRatingsDomain().max().doubleValue()) {
                            prediction = rating.getRatingValue().doubleValue() - 2;
                        }
                    } else {
                        prediction = rating.getRatingValue().doubleValue() - 2;
                        if (prediction < testDataset.getRatingsDomain().min().doubleValue()) {
                            prediction = rating.getRatingValue().doubleValue() + 2;
                        }
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
        NMAE instance = new NMAE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = 0.5f;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }

    @Test
    public void test_perfectPrediction_NMAEequals0() {
        System.out.println("test_perfectPrediction_NMAEequals0");

        //Phase 1: Preparation
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        RecommendationResults recommendationResults = new RecommendationResults();
        for (int idUser : testDataset.allUsers()) {
            try {
                ArrayList<Recommendation> recommendations = new ArrayList<>();
                for (Map.Entry<Integer, ? extends Rating> entry : testDataset.getUserRatingsRated(idUser).entrySet()) {
                    final int idItem = entry.getKey();
                    final Rating rating = entry.getValue();

                    final double prediction = rating.getRatingValue().doubleValue();
                    recommendations.add(new Recommendation(idItem, prediction));
                }
                Collections.sort(recommendations);
                recommendationResults.add(idUser, recommendations);
            } catch (UserNotFound ex) {
                //Never happens if the dataset is properly implemented
            }
        }
        NMAE instance = new NMAE();

        //Phase 2: Execution
        MeasureResult result = instance.getMeasureResult(recommendationResults, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = 0;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }
}
