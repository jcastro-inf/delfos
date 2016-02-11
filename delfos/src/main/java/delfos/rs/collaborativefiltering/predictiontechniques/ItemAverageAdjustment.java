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

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;

/**
 * Esta técnica presupone que una predicción para un usuario concreto sobre un
 * ítem es igual al valor medio de ese ítem más un ajuste que viene a ser la
 * suma ponderada de las evaluaciones hechas por el usuario y su similaridad con
 * el ítem activo. Fórmula extraida del guión de prácticas de sistemas
 * informáticos.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-Feb-2013)
 */
public class ItemAverageAdjustment extends PredictionTechnique {

    private static final long serialVersionUID = 1L;

    @Override
    public float predictRating(int idUser, int idItem, Collection<MatchRating> ratings, RatingsDataset<? extends Rating> rd) throws CouldNotPredictRating, ItemNotFound, UserNotFound {
        float prediccion;

        float numerador = 0;
        float denominador = 0;
        float itemAverage = rd.getMeanRatingItem(idItem);

        if (ratings.isEmpty()) {
            throw new CouldNotPredictRating("Match rating list is empty");
        }

        StringBuilder str = new StringBuilder();
        if (Global.isVerboseAnnoying()) {
            str.append("====================================================\n");
            str.append("userAverage(").append(idUser).append(") --> ").append(rd.getMeanRatingUser(idUser)).append("\n");
            str.append("itemAverage(").append(idItem).append(") --> ").append(rd.getMeanRatingItem(idItem)).append("\n");
            str.append("Prediciendo item ").append(idItem).append(" para user ").append(idUser).append("\n");
        }

        for (MatchRating matchRating : ratings) {
            if (Global.isVerboseAnnoying()) {
                str.append("item vecino ").append(matchRating.getIdItem()).append(" similitud ").append(matchRating.getWeight()).append(" rating ").append(matchRating.getRating()).append("\n");
            }
            numerador += (matchRating.getRating().floatValue() - rd.getMeanRatingUser(matchRating.getIdUser())) * matchRating.getWeight();
            denominador += matchRating.getWeight();
        }

        prediccion = itemAverage + (numerador / denominador);
        if (Global.isVerboseAnnoying()) {
            str.append("Predicción ").append(prediccion).append("\n");
            str.append("====================================================\n");
            Global.showInfoMessage(str.toString());
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
