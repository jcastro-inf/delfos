package delfos.group.grs.filtered.filters;

import java.util.Map;
import java.util.TreeMap;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Implementa un filtro de ratings que no elimina valoraciones.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 13-May-2013
 */
public class NoFilter extends GroupRatingsFilter {

    private static final long serialVersionUID = 1L;

    public NoFilter() {
        super();

    }

    @Override
    public Map<Integer, Map<Integer, Rating>> getFilteredRatings(RatingsDataset<? extends Rating> ratingsDataset, GroupOfUsers group) {

        //Fetch dataset.
        Map<Integer, Map<Integer, Number>> groupRatings = new TreeMap<Integer, Map<Integer, Number>>();
        for (int idUser : group) {
            try {
                groupRatings.put(idUser, new TreeMap<Integer, Number>());
                for (Map.Entry<Integer, ? extends Rating> entry : ratingsDataset.getUserRatingsRated(idUser).entrySet()) {
                    Rating rating = entry.getValue();
                    groupRatings.get(idUser).put(rating.idItem, rating.ratingValue);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        return DatasetUtilities.getMapOfMaps_Rating(groupRatings);
    }

    @Override
    public Map<Integer, Map<Integer, Number>> getFilteredRatings(Map<Integer, Map<Integer, Number>> originalSet) {

        //Copy the originalSet into ret
        Map<Integer, Map<Integer, Number>> ret = new TreeMap<Integer, Map<Integer, Number>>();

        for (Map.Entry<Integer, Map<Integer, Number>> userRatings : originalSet.entrySet()) {
            int idUser = userRatings.getKey();
            Map<Integer, Number> userRatingsMap = userRatings.getValue();

            ret.put(idUser, new TreeMap<Integer, Number>());
            for (Map.Entry<Integer, Number> entry : userRatingsMap.entrySet()) {
                int idItem = entry.getKey();
                Number rating = entry.getValue();
                ret.get(idUser).put(idItem, rating.doubleValue());
            }
        }
        return ret;
    }
}
