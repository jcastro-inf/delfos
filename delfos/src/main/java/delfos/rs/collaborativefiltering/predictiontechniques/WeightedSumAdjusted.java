package delfos.rs.collaborativefiltering.predictiontechniques;

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Implementa la técnica de predicción de la suma ponderada con normalización de
 * ratings en base a la media de los usuarios
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-Feb-2013)
 */
public class WeightedSumAdjusted extends PredictionTechnique {

    private static final long serialVersionUID = 1L;

    @Override
    public float predictRating(int idUser, int idItem, Collection<MatchRating> ratings, RatingsDataset<? extends Rating> rd) throws CouldNotPredictRating {
        float prediccion, numerador = 0, denominador = 0;

        if (ratings.isEmpty()) {
            throw new CouldNotPredictRating("Match rating list is empty");
        }
        for (MatchRating matchRating : ratings) {
            float avgRating = 0;
            switch (matchRating.getEntity()) {
                case USER:
                    try {
                        avgRating = rd.getMeanRatingUser(matchRating.getIdUser());
                    } catch (UserNotFound ex) {
                        throw new CouldNotPredictRating(ex.getMessage());
                    }
                    break;
                case ITEM:
                    try {
                        rd.getMeanRatingItem(matchRating.getIdItem());
                    } catch (ItemNotFound ex) {
                        throw new CouldNotPredictRating(ex.getMessage());
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Entity type isnt a user or item: " + matchRating.getEntity());
            }

            numerador += (matchRating.getRating().floatValue() - avgRating) * matchRating.getWeight();
            denominador += matchRating.getWeight();
        }

        try {
            prediccion = rd.getMeanRatingUser(idUser) + (numerador / denominador);
        } catch (UserNotFound ex) {
            throw new CouldNotPredictRating(ex.getMessage());
        }

        if (Float.isInfinite(prediccion)) {
            throw new CouldNotPredictRating("Prediction is infinite");
        }
        if (Float.isNaN(prediccion)) {
            throw new CouldNotPredictRating("Prediction is NaN");
        }

        return prediccion;
    }
}
