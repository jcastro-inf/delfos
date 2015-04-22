package delfos.group.results.groupevaluationmeasures;

import delfos.common.Global;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

/**
 * Medida de evaluación para calcular el número medio de solicitudes de
 * predicción que se hicieron al sistema por grupo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 14-Mayo-2013
 * @see Coverage_ForGroups
 */
public class AverageNumberOfRequests extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        MeanIterative mean = new MeanIterative();
        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            GroupOfUsers group = entry.getKey();
            Collection<Integer> requestsToGroup = recommendationResults.getRequests(group);
            if (requestsToGroup == null) {
                Global.showWarning("the group " + group + " has no requests (null), seedOfExecution: " + recommendationResults.getSeed());
                mean.addValue(0);
            } else {
                mean.addValue(requestsToGroup.size());
            }
        }
        return new GroupMeasureResult(this, mean.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
