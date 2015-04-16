package delfos.group.results.groupevaluationmeasures;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.rs.recommendation.Recommendation;
import delfos.common.Global;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;

/**
 * Medida de evaluación para calcular la cobertura del sistema de recomendación
 * evaluado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.Coverage} para
 * recomendaciones individuales.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 (26-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE_ForGroups
 */
public class Coverage extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        int predichas = 0;
        int solicitudes = 0;

        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            GroupOfUsers group = entry.getKey();
            List<Recommendation> recommendationsToGroup = entry.getValue();

            {
                //Compruebo que no hay recomendaciones repetidas.
                Set<Integer> itemsRecomendados = new TreeSet<Integer>();
                for (Recommendation r : recommendationsToGroup) {
                    if (itemsRecomendados.contains(r.getIdItem())) {
                        Global.showWarning("The group " + group + " has received item " + r.getIdItem() + " as recommendation multiple times.");
                    } else {
                        itemsRecomendados.add(r.getIdItem());
                    }
                }
            }

            Collection<Integer> solicitadas = recommendationResults.getRequests(group);
            if (solicitadas == null) {
                Global.showWarning("the group " + group + " has no requests (null), seedOfExecution: " + recommendationResults.getSeed());
                recommendationResults.getRequests(group);
            } else {
                solicitudes += solicitadas.size();
                predichas += recommendationsToGroup.size();
            }
        }
        float ret = predichas / ((float) solicitudes);
        return new GroupMeasureResult(this, ret);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
