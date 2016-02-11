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
package delfos.group.grs.aggregation;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import delfos.dataset.basic.rating.Rating;

/**
 * Modelo de un grupo que almacena las valoraciones filtradas de los miembros.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 29-May-2013
 */
public class GroupModelRatingsPreFilter implements Serializable {

    private static final long serialVersionUID = 123L;

    private final Map<Integer, Map<Integer, Rating>> ratings;
    private final Object innerGRSGroupModel;

    public GroupModelRatingsPreFilter(Map<Integer, Map<Integer, Rating>> filteredRatings, Object innerGRSGroupModel) {
        this.ratings = filteredRatings;
        this.innerGRSGroupModel = innerGRSGroupModel;
    }

    public Map<Integer, Map<Integer, Rating>> getRatings() {
        return Collections.unmodifiableMap(ratings);
    }

    public Object getInnerGRSGroupModel() {
        return innerGRSGroupModel;
    }
}
