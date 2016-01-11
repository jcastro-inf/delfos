package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Medida de evaluación para calcular la raiz del error cuadrático medio
 * normalizado del sistema de recomendación evaluado. Calcula la diferencia
 * entre la valoración hecha para el grupo y la valoración individual que cada
 * usuario dió para el producto, si lo ha valorado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.RatingPrediction.NRMSE} para
 * recomendaciones individuales.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 19-Febrero-2014
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.NMAE
 */
public class NRMSE extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative nrmse = new MeanIterative();

        Domain originalDomain = testDataset.getRatingsDomain();

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            Map<Integer, Map<Integer, ? extends Rating>> groupTrueRatings = new TreeMap<>();
            for (int idUser : groupOfUsers.getIdMembers()) {
                try {
                    groupTrueRatings.put(idUser, testDataset.getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }

            for (Recommendation r : groupRecommendations) {
                int idItem = r.getIdItem();
                for (int idUser : groupOfUsers.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(idItem)) {
                        double trueRating = groupTrueRatings.get(idUser).get(idItem).getRatingValue().doubleValue();
                        double predictedRating = r.getPreference().doubleValue();

                        double trueRatingNormalised = originalDomain.convertToDecimalDomain(trueRating, DecimalDomain.ZERO_TO_ONE).doubleValue();
                        double predictedNormalised = originalDomain.convertToDecimalDomain(predictedRating, DecimalDomain.ZERO_TO_ONE).doubleValue();

                        nrmse.addValue(Math.pow(predictedNormalised - trueRatingNormalised, 2));
                    }
                }
            }
        }

        if (nrmse.getNumValues() == 0) {
            return new GroupEvaluationMeasureResult(this, Double.NaN);
        } else {
            float rmseValue = (float) Math.sqrt(nrmse.getMean());
            return new GroupEvaluationMeasureResult(this, rmseValue);
        }

    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
