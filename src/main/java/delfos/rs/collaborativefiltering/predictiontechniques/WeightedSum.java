/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.rs.collaborativefiltering.predictiontechniques;

import delfos.common.exceptions.CouldNotPredictRating;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import java.util.Collection;

/**
 * Clase que implementa la técnica de predicción de la suma ponderada. Si se
 * invocan los métodos que carecen de pesos ( <code>weights</code>) supone que
 * los datos tienen el mismo peso
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-Feb-2013)
 */
public class WeightedSum extends PredictionTechnique {

    private static final long serialVersionUID = 1L;

    @Override
    public double predictRating(int idUser, int idItem, Collection<MatchRating> ratings, RatingsDataset<? extends Rating> rd) throws CouldNotPredictRating {
        double numerador = 0, denominador = 0;

        if (ratings.isEmpty()) {
            throw new CouldNotPredictRating("Match rating list is empty");
        }
        for (MatchRating matchRating : ratings) {
            numerador += matchRating.getRating().doubleValue() * matchRating.getWeight();
            denominador += matchRating.getWeight();
        }

        double prediccion = numerador / denominador;

        if (Double.isInfinite(prediccion)) {
            throw new CouldNotPredictRating("Prediction is infinite");
        }
        if (Double.isNaN(prediccion)) {
            throw new CouldNotPredictRating("Prediction is NaN");
        }

        return prediccion;
    }
}
