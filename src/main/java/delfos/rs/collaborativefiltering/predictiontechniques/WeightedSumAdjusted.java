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
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import java.util.Collection;

/**
 * Implementa la técnica de predicción de la suma ponderada con normalización de
 * ratings en base a la media de los usuarios
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-Feb-2013)
 */
public class WeightedSumAdjusted extends PredictionTechnique {

    private static final long serialVersionUID = 1L;

    @Override
    public double predictRating(long idUser, long idItem, Collection<MatchRating> ratings, RatingsDataset<? extends Rating> rd) throws CouldNotPredictRating {
        double prediccion, numerador = 0, denominador = 0;

        if (ratings.isEmpty()) {
            throw new CouldNotPredictRating("Match rating list is empty");
        }
        for (MatchRating matchRating : ratings) {
            double avgRating = 0;
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

            numerador += (matchRating.getRating().doubleValue() - avgRating) * matchRating.getWeight();
            denominador += matchRating.getWeight();
        }

        try {
            prediccion = rd.getMeanRatingUser(idUser) + (numerador / denominador);
        } catch (UserNotFound ex) {
            throw new CouldNotPredictRating(ex.getMessage());
        }

        if (Double.isInfinite(prediccion)) {
            throw new CouldNotPredictRating("Prediction is infinite");
        }
        if (Double.isNaN(prediccion)) {
            throw new CouldNotPredictRating("Prediction is NaN");
        }

        return prediccion;
    }
}
