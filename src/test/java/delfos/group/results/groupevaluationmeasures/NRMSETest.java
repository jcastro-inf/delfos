package delfos.group.results.groupevaluationmeasures;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Implementa tests para {@link NRMSE}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 19-febrero-2014
 */
public class NRMSETest {

    public NRMSETest() {
    }

    /**
     * Test of getMeasureResult method, of class RMSE.
     */
    @Test
    public void test_noRecommendations_RMSEequalsNaN() {

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

        GroupOfUsers groupOfUsers = new GroupOfUsers(1l, 2l);
        Set<Item> groupRequests = Arrays.asList(new Item(12), new Item(13)).stream().collect(Collectors.toSet());

        List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs = Arrays.asList(
                new SingleGroupRecommendationTaskInput(null, datasetLoader, recommendationModel, groupOfUsers, groupRequests));

        GroupRecommendations groupRecommendations = new GroupRecommendations(groupOfUsers, Collections.EMPTY_LIST);
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
                0, -1);

        NRMSE instance = new NRMSE();

        //Phase 2: Execution
        GroupEvaluationMeasureResult groupMaeResult = instance.getMeasureResult(
                groupRecommenderSystemResult,
                datasetLoader,
                relevanceCriteria,
                datasetLoader,
                datasetLoader);

        //Phase 3: Result checking
        double expResult = Double.NaN;
        double delta = 0.001f;
        assertEquals(expResult, groupMaeResult.getValue(), delta);
    }
}
