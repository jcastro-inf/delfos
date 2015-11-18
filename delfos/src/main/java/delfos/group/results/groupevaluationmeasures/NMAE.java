package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.rs.recommendation.Recommendation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Medida de evaluación para calcular el error absoluto medio normalizado del
 * sistema de recomendación evaluado. Calcula la diferencia entre la valoración
 * hecha para el grupo y la valoración individual que cada usuario dió para el
 * producto, si lo ha valorado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.RatingPrediction.NMAE} para
 * recomendaciones individuales.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 19-Febrero-2014
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.NMAE
 */
public class NMAE extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        MeanIterative nmae = new MeanIterative();

        Domain originalDomain = testDataset.getRatingsDomain();

        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            GroupOfUsers group = entry.getKey();
            List<Recommendation> recommendationsToGroup = entry.getValue();

            Map<Integer, Map<Integer, ? extends Rating>> groupTrueRatings = new TreeMap<>();
            for (int idUser : group.getIdMembers()) {
                try {
                    groupTrueRatings.put(idUser, testDataset.getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }

            for (Recommendation r : recommendationsToGroup) {
                int idItem = r.getIdItem();
                for (int idUser : group.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(idItem)) {
                        double trueRating = groupTrueRatings.get(idUser).get(idItem).getRatingValue().doubleValue();
                        double predictedRating = r.getPreference().doubleValue();

                        double trueRatingNormalised = originalDomain.convertToDecimalDomain(trueRating, DecimalDomain.ZERO_TO_ONE).doubleValue();
                        double predictedNormalised = originalDomain.convertToDecimalDomain(predictedRating, DecimalDomain.ZERO_TO_ONE).doubleValue();

                        nmae.addValue(Math.abs(predictedNormalised - trueRatingNormalised));
                    }
                }
            }
        }

        if (nmae.getNumValues() == 0) {
            return new GroupEvaluationMeasureResult(this, Double.NaN);
        } else {
            float rmseValue = (float) nmae.getMean();
            return new GroupEvaluationMeasureResult(this, rmseValue);
        }
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
