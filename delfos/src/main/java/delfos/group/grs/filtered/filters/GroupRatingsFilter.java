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
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Establece los métodos que debe implementar un algoritmo de filtrado de
 * valoraciones para sistemas de recomendación a grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 07-May-2013
 * @version 1.1 22-May-2013 Modificados los métodos que provee para soportar
 * filtrado de recomendaciones.
 */
public abstract class GroupRatingsFilter extends ParameterOwnerAdapter {

    public GroupRatingsFilter() {
        super();
    }

    /**
     * Realiza el filtrado de las valoraciones del grupo, devolviendo las
     * valoraciones finales que el sistema de recomendación debe utilizar.
     *
     * @param ratingsDataset Dataset de valoraciones.
     * @param group Grupo para el que se calcula el conjunto de valoraciones.
     * @return Devuelve las valoraciones del grupo que se utilizan.
     */
    public Map<Integer, Map<Integer, Rating>> getFilteredRatings(RatingsDataset<? extends Rating> ratingsDataset, GroupOfUsers group) {
        Map<Integer, Map<Integer, Number>> groupRatings = new TreeMap<>();
        for (int idUser : group) {
            try {
                Map<Integer, ? extends Rating> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);

                Map<Integer, Number> userRatings_Number = new TreeMap<>();
                for (Rating rating : userRatingsRated.values()) {
                    userRatings_Number.put(rating.getIdItem(), rating.getRatingValue());
                }
                groupRatings.put(idUser, userRatings_Number);
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        Map<Integer, Map<Integer, Number>> filteredRatingsByUser = getFilteredRatings(groupRatings);

        return DatasetUtilities.getMapOfMaps_Rating(filteredRatingsByUser);
    }

    /**
     * Realiza el filtrado de las valoraciones indicadas, indexadas por usuario.
     * Devuelve las valoraciones filtradas.
     *
     * @param ratingsByUser Dataset de valoraciones.
     *
     * @return Devuelve las valoraciones filtradas.
     */
    public abstract Map<Integer, Map<Integer, Number>> getFilteredRatings(Map<Integer, Map<Integer, Number>> ratingsByUser);

    @Override
    public boolean equals(Object obj) {
        return this.getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_RATINGS_FILTER;
    }

}
