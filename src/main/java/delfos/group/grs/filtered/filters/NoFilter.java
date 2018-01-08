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
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Implementa un filtro de ratings que no elimina valoraciones.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 13-May-2013
 */
public class NoFilter extends GroupRatingsFilter {

    private static final long serialVersionUID = 1L;

    public NoFilter() {
        super();

    }

    @Override
    public Map<Long, Map<Long, Rating>> getFilteredRatings(RatingsDataset<? extends Rating> ratingsDataset, GroupOfUsers group) {

        //Fetch dataset.
        Map<Long, Map<Long, Number>> groupRatings = new TreeMap<Long, Map<Long, Number>>();
        for (Long idUser : group) {
            try {
                groupRatings.put(idUser, new TreeMap<Long, Number>());
                for (Map.Entry<Long, ? extends Rating> entry : ratingsDataset.getUserRatingsRated(idUser).entrySet()) {
                    Rating rating = entry.getValue();
                    groupRatings.get(idUser).put(rating.getIdItem(), rating.getRatingValue());
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        return DatasetUtilities.getMapOfMaps_Rating(groupRatings);
    }

    @Override
    public Map<Long, Map<Long, Number>> getFilteredRatings(Map<Long, Map<Long, Number>> originalSet) {

        //Copy the originalSet into ret
        Map<Long, Map<Long, Number>> ret = new TreeMap<Long, Map<Long, Number>>();

        for (Map.Entry<Long, Map<Long, Number>> userRatings : originalSet.entrySet()) {
            Long idUser = userRatings.getKey();
            Map<Long, Number> userRatingsMap = userRatings.getValue();

            ret.put(idUser, new TreeMap<Long, Number>());
            for (Map.Entry<Long, Number> entry : userRatingsMap.entrySet()) {
                Long idItem = entry.getKey();
                Number rating = entry.getValue();
                ret.get(idUser).put(idItem, rating.doubleValue());
            }
        }
        return ret;
    }
}
