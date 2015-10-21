package delfos.group.results.groupevaluationmeasures;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Implementa tests para {@link NRMSE}.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
        long seed = 0;
        long buildTime = 0;
        long groupBuildTime = 0;
        long groupRecommendationTime = 0;
        GroupOfUsers groupOfUsers = new GroupOfUsers(1, 2);
        RatingsDataset<? extends Rating> testDataset = new RatingsDatasetMock();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);

        Map<GroupOfUsers, Collection<Integer>> requests = new TreeMap<>();
        requests.put(groupOfUsers, Arrays.asList(12, 13));

        Map<GroupOfUsers, Collection<Recommendation>> results = new TreeMap<>();
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        results.put(groupOfUsers, recommendations);

        GroupRecommendationResult groupRecommendationResult = new GroupRecommendationResult(seed, buildTime, groupBuildTime, groupRecommendationTime, requests, results, "TestCaseAlias");
        groupRecommendationResult.add(groupOfUsers, new ArrayList<>());
        NRMSE instance = new NRMSE();

        //Phase 2: Execution
        GroupMeasureResult result = instance.getMeasureResult(groupRecommendationResult, testDataset, relevanceCriteria);

        //Phase 3: Result checking
        float expResult = Float.NaN;
        float delta = 0.001f;
        assertEquals(expResult, result.getValue(), delta);
    }
}
