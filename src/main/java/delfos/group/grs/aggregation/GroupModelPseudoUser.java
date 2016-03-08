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

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Modelo de un grupo que almacena la valoración del grupo para cada producto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 29-May-2013
 * @param <RatingType>
 */
public class GroupModelPseudoUser<RatingType extends Rating> implements Serializable {

    private static final long serialVersionUID = 124L;

    private final GroupOfUsers group;
    private final Map<Item, RatingType> ratings;

    public GroupModelPseudoUser(GroupOfUsers group, Map<Item, RatingType> ratings) {

        this.group = group;
        this.ratings = ratings;
    }

    public Map<Item, RatingType> getRatings() {
        return Collections.unmodifiableMap(ratings);
    }

    public GroupOfUsers getGroup() {
        return group;
    }
}
