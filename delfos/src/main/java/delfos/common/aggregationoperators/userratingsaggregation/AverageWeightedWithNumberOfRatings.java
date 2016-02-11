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
package delfos.common.aggregationoperators.userratingsaggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.common.aggregationoperators.weighted.WeightedSumAggregation;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Agrega las valoraciones de un grupo de usuarios sobre un producto indicado.
 * Utiliza como ponderación el número de ratings de cada usuario, ya que cuanto
 * más activo es, más influyente es y tendrá un mayor peso en la agregación.
 *
 * <p>
 * <p>
 * Shlomo Berkovsky, Jill Freyne: Group-based recipe recommendations: analysis
 * of data aggregation strategies. RecSys '10 Proceedings of the fourth ACM
 * conference on Recommender systems Pages 111-118 ACM New York, NY, USA.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 04-Julio-2013
 */
public class AverageWeightedWithNumberOfRatings implements UserRatingsAggregation {

    private final WeightedSumAggregation ws = new WeightedSumAggregation();

    @Override
    public Number aggregateRatings(RatingsDataset<? extends Rating> rd, Collection<Integer> users, int idItem) throws UserNotFound, ItemNotFound {
        List<Number> values = new ArrayList<Number>(users.size());
        List<Double> weights = new ArrayList<Double>(users.size());
        double norma = 0;
        for (int idUser : users) {
            Number userRating = rd.getRating(idUser, idItem).getRatingValue();
            values.add(userRating);
            double sizeOfUserRatings = rd.sizeOfUserRatings(idUser);
            weights.add(sizeOfUserRatings);
            norma += sizeOfUserRatings;
        }
        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) / norma);
        }
        return ws.aggregateValues(values, weights);
    }
}
