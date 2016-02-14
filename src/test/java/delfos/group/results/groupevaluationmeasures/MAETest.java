package delfos.group.results.groupevaluationmeasures;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
     * Test of getMeasureResult method, of class RMSE.
     */
    @Test
    public void test_noRecommendations_MAEEqualsNaN() {

        //Phase 1: Preparation
        final RatingsDataset<Rating> testDataset = new RatingsDatasetMock();

        DatasetLoader<? extends Rating> datasetLoader = new CompleteDatasetLoaderAbstract<Rating>() {

            @Override
            public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
                return testDataset;
            }
        };

        Object recommendationModel = null;

        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);

        GroupOfUsers groupOfUsers = new GroupOfUsers(1, 2);
        Set<Integer> groupRequests = Arrays.asList(12, 13).stream().collect(Collectors.toSet());

        List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs = Arrays.asList(
                new SingleGroupRecommendationTaskInput(null, datasetLoader, recommendationModel, groupOfUsers, groupRequests));

        Collection<Recommendation> groupRecommendations = Collections.EMPTY_LIST;
        long buildTime = 0;
        long groupBuildTime = 0;
        long groupRecommendationTime = 0;
        List<SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs = Arrays.asList(
                new SingleGroupRecommendationTaskOutput(groupOfUsers, groupRecommendations, buildTime, groupRecommendationTime));

        GroupRecommenderSystemResult groupRecommenderSystemResult = new GroupRecommenderSystemResult(
                singleGroupRecommendationInputs,
                singleGroupRecommendationOutputs,
                MAETest.class.getSimpleName(),
                0,
                0);

        MAE instance = new MAE();

        //Phase 2: Execution
        GroupEvaluationMeasureResult groupMaeResult = instance.getMeasureResult(
                groupRecommenderSystemResult,
                datasetLoader,
                testDataset,
                relevanceCriteria,
                datasetLoader,
                datasetLoader);

        //Phase 3: Result checking
        float expResult = Float.NaN;
        float delta = 0.001f;
        assertEquals(expResult, groupMaeResult.getValue(), delta);
    }

}
