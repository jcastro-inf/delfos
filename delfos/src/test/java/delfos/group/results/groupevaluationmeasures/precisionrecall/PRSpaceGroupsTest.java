package delfos.group.results.groupevaluationmeasures.precisionrecall;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatrix;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 15-Jan-2013
 */
public class PRSpaceGroupsTest {

    public PRSpaceGroupsTest() {
    }

    /**
     * Test of getMeasureResult method, of class PRSpaceGroups.
     */
    @Test
    public void testGetMeasureResult() {

        List<Rating> ratings = new ArrayList<>(20);

        ratings.add(new Rating(1, 1, 4));
        ratings.add(new Rating(1, 2, 4));
        ratings.add(new Rating(1, 3, 4));
        ratings.add(new Rating(1, 4, 4));
        ratings.add(new Rating(1, 5, 2));

        ratings.add(new Rating(2, 1, 2));
        ratings.add(new Rating(2, 2, 4));
        ratings.add(new Rating(2, 3, 4));
        ratings.add(new Rating(2, 4, 4));
        ratings.add(new Rating(2, 5, 4));

        ratings.add(new Rating(3, 1, 4));
        ratings.add(new Rating(3, 2, 4));
        ratings.add(new Rating(3, 3, 2));
        ratings.add(new Rating(3, 4, 4));
        ratings.add(new Rating(3, 5, 4));

        RatingsDataset<? extends Rating> ratingsDataset = new BothIndexRatingsDataset(ratings);

        GroupOfUsers groupOne = new GroupOfUsers();
        groupOne.addUser(1);
        groupOne.addUser(2);
        groupOne.addUser(3);

        Map<GroupOfUsers, Collection<Recommendation>> recommendations_byGroup = new TreeMap<>();
        Map<GroupOfUsers, Collection<Integer>> requests_byGroup = new TreeMap<>();

        Collection<Recommendation> recommendations = new ArrayList<>();
        recommendations.add(new Recommendation(2, 5));
        recommendations.add(new Recommendation(4, 4.9));
        recommendations.add(new Recommendation(1, 4.8));
        recommendations.add(new Recommendation(3, 4.7));
        recommendations.add(new Recommendation(5, 4.6));

        List<Integer> requests = new LinkedList<>();
        requests.add(1);
        requests.add(2);
        requests.add(3);
        requests.add(4);
        requests.add(5);

        requests_byGroup.put(groupOne, requests);
        recommendations_byGroup.put(groupOne, recommendations);

        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);
        PRSpaceGroups instance = new PRSpaceGroups();

        ConfusionMatrix[] matrices = new ConfusionMatrix[6];

        matrices[0] = new ConfusionMatrix(0, 2, 0, 3);
        matrices[1] = new ConfusionMatrix(0, 1, 1, 3);
        matrices[2] = new ConfusionMatrix(0, 0, 2, 3);
        matrices[3] = new ConfusionMatrix(1, 0, 2, 2);
        matrices[4] = new ConfusionMatrix(2, 0, 2, 1);
        matrices[5] = new ConfusionMatrix(3, 0, 2, 0);

        GroupEvaluationMeasureResult expResult = new GroupEvaluationMeasureResult(instance, 1, ConfusionMatricesCurveXML.getElement(new ConfusionMatricesCurve(matrices)));

        //TODO: Finalizar test.
        // TODO implement this test
        if (1 == 1) {
            fail("You must implement this test.");
            return;
        }
    }
}
