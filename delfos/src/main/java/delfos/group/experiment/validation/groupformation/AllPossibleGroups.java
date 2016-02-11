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
package delfos.group.experiment.validation.groupformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Devuelve todos los grupos posibles del dataset. Si el dataset tiene más de 10
 * usuarios, no se puede aplicar esta validación.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 24-May-2013
 */
public class AllPossibleGroups extends GroupFormationTechnique {

    @Override
    public Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {

        Collection<Integer> allUsers = datasetLoader.getRatingsDataset().allUsers();
        if (allUsers.size() > 20) {
            throw new IllegalArgumentException("The number of users cannot be greater than 20.");
        }

        double numberOfDifferentGroups = Math.pow(2, allUsers.size());
        Set<GroupOfUsers> groupsSet = new HashSet<GroupOfUsers>((int) numberOfDifferentGroups);

        for (int idUser : allUsers) {
            groupsSet.add(new GroupOfUsers(idUser));
        }

        for (int idUser : allUsers) {
            Set<GroupOfUsers> gruposAAñadir = new TreeSet<GroupOfUsers>(groupsSet);

            for (GroupOfUsers groupOfUsers : gruposAAñadir) {
                Set<Integer> members = new TreeSet<Integer>(groupOfUsers.getIdMembers());

                members.add(idUser);
                groupsSet.add(new GroupOfUsers(members.toArray(new Integer[0])));

            }
        }

        return new ArrayList<GroupOfUsers>(groupsSet);
    }
}
