package delfos.group.results.groupevaluationmeasures;

import delfos.common.Global;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import java.util.Collection;

/**
 * Medida de evaluación para calcular el número medio de solicitudes de
 * predicción que se hicieron al sistema por grupo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 14-Mayo-2013
 * @see Coverage_ForGroups
 */
public class GroupAverageNumberOfRequests extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(GroupRecommenderSystemResult groupRecommenderSystemResult, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        MeanIterative mean = new MeanIterative();
        for (GroupOfUsers group : groupRecommenderSystemResult) {
            Collection<Integer> requestsToGroup = groupRecommenderSystemResult.getGroupInput(group).getItemsRequested();
            if (requestsToGroup == null) {
                Global.showWarning("the group " + group + " has no requests (null)");
                mean.addValue(0);
            } else {
                mean.addValue(requestsToGroup.size());
            }
        }
        return new GroupEvaluationMeasureResult(this, mean.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
