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
package delfos.group.experiment.validation.recommendableitems;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Objeto que calcula los items que se pueden recomendar a un grupo de usuarios,
 * dependiendo de los items que cada miembro ha valorado. Esta validación
 * considera que sólo se deben recomendar productos que ningún usuario haya
 * experimentado previamente.
 *
* @author Jorge Castro Gallardo
 */
public class NeverRatedItems extends RecomendableItemTechnique {

    @Override
    public Collection<Integer> getRecommendableItems(GroupOfUsers groupOfUsers, RatingsDataset<? extends Rating> ratingsDataset, ContentDataset contentDataset) {
        Set<Integer> ret = new TreeSet<Integer>(contentDataset.allID());

        for (int idUser : groupOfUsers.getIdMembers()) {
            try {
                ret.removeAll(ratingsDataset.getUserRated(idUser));
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        return ret;
    }
}
