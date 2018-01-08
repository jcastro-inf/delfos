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
package delfos.group.grs.filtered.filters;

import java.util.Map;
import java.util.TreeMap;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.common.statisticalfuncions.StandardDeviation;
import delfos.dataset.util.DatasetUtilities;

/**
 * Implementa un filtro de ratings que elimina los ratings que son demasiado
 * distintos al resto de ratings del grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 03-May-2013
 */
public class OutliersRatingsStandardDeviationFilter extends GroupRatingsFilter {

    private static final long serialVersionUID = 1L;

    public OutliersRatingsStandardDeviationFilter() {
        super();
    }

    public static boolean valueInNormalDistribution(double mean, double standardDeviation, double value) {
        return mean - standardDeviation <= value && value <= mean + standardDeviation;
    }

    @Override
    public Map<Long, Map<Long, Number>> getFilteredRatings(Map<Long, Map<Long, Number>> ratingsByUser) {

        Map<Long, Map<Long, Number>> ratingsByItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(ratingsByUser);

        Map<Long, Map<Long, Number>> filteredRatingsByItem = new TreeMap<>();

        for (long idItem : ratingsByItem.keySet()) {

            double standardDeviation = new StandardDeviation(ratingsByItem.get(idItem).values()).getStandardDeviation();
            double mean = new MeanIterative(ratingsByItem.get(idItem).values()).getMean();

            filteredRatingsByItem.put(idItem, new TreeMap<>());

            for (Map.Entry<Long, Number> entry : ratingsByItem.get(idItem).entrySet()) {
                long idUser = entry.getKey();
                Number rating = entry.getValue();

                if (valueInNormalDistribution(mean, standardDeviation, rating.doubleValue())) {
                    filteredRatingsByItem.get(idItem).put(idUser, rating);
                }
            }
        }

        Map<Long, Map<Long, Number>> filteredRatingsByUser
                = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(filteredRatingsByItem);
        return filteredRatingsByUser;
    }
}
