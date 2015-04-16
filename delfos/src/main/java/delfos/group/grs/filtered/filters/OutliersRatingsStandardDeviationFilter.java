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
* @author Jorge Castro Gallardo
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
    public Map<Integer, Map<Integer, Number>> getFilteredRatings(Map<Integer, Map<Integer, Number>> ratingsByUser) {

        Map<Integer, Map<Integer, Number>> ratingsByItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(ratingsByUser);

        Map<Integer, Map<Integer, Number>> filteredRatingsByItem = new TreeMap<>();

        for (int idItem : ratingsByItem.keySet()) {

            double standardDeviation = new StandardDeviation(ratingsByItem.get(idItem).values()).getStandardDeviation();
            double mean = new MeanIterative(ratingsByItem.get(idItem).values()).getMean();

            filteredRatingsByItem.put(idItem, new TreeMap<>());

            for (Map.Entry<Integer, Number> entry : ratingsByItem.get(idItem).entrySet()) {
                int idUser = entry.getKey();
                Number rating = entry.getValue();

                if (valueInNormalDistribution(mean, standardDeviation, rating.doubleValue())) {
                    filteredRatingsByItem.get(idItem).put(idUser, rating);
                }
            }
        }

        Map<Integer, Map<Integer, Number>> filteredRatingsByUser
                = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(filteredRatingsByItem);
        return filteredRatingsByUser;
    }
}
