package delfos.group.results.groupevaluationmeasures;

import delfos.common.Global;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import org.jdom2.Element;

/**
 * Medida de evaluación para calcular el número de solicitudes de predicción que
 * se hicieron al sistema.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 14-Mayo-2013
 * @see Coverage_ForGroups
 */
public class NumberOfRequests extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        Element ret = ParameterOwnerXML.getElement(this);
        int solicitadas = 0;
        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            GroupOfUsers group = entry.getKey();
            Collection<Integer> requestsToGroup = recommendationResults.getRequests(group);
            Element groupRequests = new Element("GroupRequests");
            groupRequests.setAttribute("group", group.toString());
            if (requestsToGroup == null) {
                Global.showWarning("the group " + group + " has no requests (null), seedOfExecution: " + recommendationResults.getSeed());
                groupRequests.addContent("[]");
                recommendationResults.getRequests(group);
            } else {
                solicitadas += requestsToGroup.size();
                groupRequests.addContent(requestsToGroup.toString());
            }
            ret.addContent(groupRequests);
        }
        ret.setAttribute("value", Integer.toString(solicitadas));
        return new GroupMeasureResult(this, solicitadas, ret);
    }

    @Override
    public GroupMeasureResult agregateResults(Collection<GroupMeasureResult> results) {
        float sumOfAggregated = 0;
        for (GroupMeasureResult mr : results) {
            double value = mr.getValue();
            if (Double.isNaN(value)) {
                Global.showWarning("The value for the measure " + this.getName() + " is NaN");
            } else {
                if (Double.isInfinite(value)) {
                    Global.showWarning("The value for the measure " + this.getName() + " is Infinite");
                } else {
                    sumOfAggregated += mr.getValue();
                }
            }
        }
        return new GroupMeasureResult(this, sumOfAggregated);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
