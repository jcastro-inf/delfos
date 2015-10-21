package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Medida de evaluación para calcular el error absoluto medio del sistema de
 * recomendación evaluado. Calcula la diferencia entre la valoración hecha para
 * el grupo y la valoración individual que cada usuario dió para el producto, si
 * lo ha valorado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.RatingPrediction.MAE} para
 * recomendaciones individuales.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (10-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE
 */
public class MAE extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        //Acumula todos los errores de predicción cometidos.
        double accumulatedAbsoluteError = 0;

        //Cuenta el número de errores que se calculan.
        long numRecommendations = 0;

        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            GroupOfUsers group = entry.getKey();
            Collection<Recommendation> recommendationsToGroup = entry.getValue();

            Map<Integer, Map<Integer, ? extends Rating>> groupTrueRatings = new TreeMap<>();
            group.getIdMembers().stream().forEach((idUser) -> {
                try {
                    groupTrueRatings.put(idUser, testDataset.getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            });

            for (Recommendation r : recommendationsToGroup) {
                int idItem = r.getIdItem();
                for (int idUser : group.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(idItem)) {
                        double trueRating = groupTrueRatings.get(idUser).get(idItem).getRatingValue().doubleValue();
                        double predicted = r.getPreference().doubleValue();
                        accumulatedAbsoluteError += Math.abs(predicted - trueRating);
                        numRecommendations++;
                    }
                }
            }
        }
        if (numRecommendations == 0) {
            return new GroupMeasureResult(this, Double.NaN);
        } else {

            float mae = (float) (accumulatedAbsoluteError / numRecommendations);
            return new GroupMeasureResult(this, mae);
        }
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
