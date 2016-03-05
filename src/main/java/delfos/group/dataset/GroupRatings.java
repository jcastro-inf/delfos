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
package delfos.group.dataset;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Modelo de un grupo que almacena la valoración del grupo para cada producto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 29-May-2013
 */
public class GroupRatings implements Serializable {

    private static final long serialVersionUID = 124L;

    private final Map<User, Map<Item, Rating>> ratings;
    private final GroupOfUsers group;

    private GroupRatings() {
        this.ratings = null;
        this.group = null;
    }

    public GroupRatings(GroupOfUsers group, Map<User, Map<Item, Rating>> ratings) {
        this.ratings = ratings;
        this.group = group;
    }

    public GroupRatings(GroupOfUsers group, Collection<Rating> groupRatings) {

        ratings = new TreeMap<>();

        for (Rating rating : groupRatings) {
            User member = rating.getUser();
            Item item = rating.getItem();

            if (!ratings.containsKey(member)) {
                ratings.put(member, new TreeMap<>());
            }

            ratings.get(member).put(item, rating);
        }
        this.group = group;
    }

    public GroupOfUsers getGroup() {
        return group;
    }
}
