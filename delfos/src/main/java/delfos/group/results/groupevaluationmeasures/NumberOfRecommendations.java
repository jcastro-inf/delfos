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
 * Medida de evaluación para calcular el número de predicciones que se
 * calcularon.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (26-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE_ForGroups
 */
public class NumberOfRecommendations extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        Element ret = ParameterOwnerXML.getElement(this);
        int recomendadas = 0;
        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            GroupOfUsers group = entry.getKey();
            Collection<Recommendation> recommendations = entry.getValue();
            Element groupRequests = new Element("GroupRecommendations");
            groupRequests.setAttribute("group", group.toString());
            if (recommendations == null) {
                Global.showWarning("the group " + group + " has no recommendations (null), seedOfExecution: " + recommendationResults.getSeed());
                groupRequests.addContent("[]");
                recommendationResults.getRequests(group);
            } else {
                recomendadas += recommendations.size();
                groupRequests.addContent(recommendations.toString());
            }
            ret.addContent(groupRequests);
        }
        ret.setAttribute("value", Integer.toString(recomendadas));
        return new GroupMeasureResult(this, recomendadas, ret);
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
