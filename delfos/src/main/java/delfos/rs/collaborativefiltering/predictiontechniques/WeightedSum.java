package delfos.rs.collaborativefiltering.predictiontechniques;

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.common.exceptions.CouldNotPredictRating;

/**
 * Clase que implementa la técnica de predicción de la suma ponderada. Si se
 * invocan los métodos que carecen de pesos ( <code>weights</code>) supone que
 * los datos tienen el mismo peso
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-Feb-2013)
 */
public class WeightedSum extends PredictionTechnique {

    private static final long serialVersionUID = 1L;

    @Override
    public float predictRating(int idUser, int idItem, Collection<MatchRating> ratings, RatingsDataset<? extends Rating> rd) throws CouldNotPredictRating {
        float numerador = 0, denominador = 0;

        if (ratings.isEmpty()) {
            throw new CouldNotPredictRating("Match rating list is empty");
        }
        for (MatchRating matchRating : ratings) {
            numerador += matchRating.getRating().floatValue() * matchRating.getWeight();
            denominador += matchRating.getWeight();
        }

        float prediccion = numerador / denominador;

        if (Float.isInfinite(prediccion)) {
            throw new CouldNotPredictRating("Prediction is infinite");
        }
        if (Float.isNaN(prediccion)) {
            throw new CouldNotPredictRating("Prediction is NaN");
        }

        return prediccion;
    }
}
