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

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Devuelve todos los grupos posibles del dataset. Si el dataset tiene más de 10 usuarios, no se puede aplicar esta
 * validación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 24-May-2013
 */
public class AllPossibleGroups extends GroupFormationTechnique {

    @Override
    public Collection<GroupOfUsers> generateGroups(DatasetLoader<? extends Rating> datasetLoader, Collection<User> usersAllowed) throws CannotLoadRatingsDataset {

        Collection<User> allUsers = datasetLoader.getRatingsDataset().allUsers().stream().map(idUser -> datasetLoader.getUsersDataset().get(idUser)).collect(Collectors.toList());
        if (allUsers.size() > 20) {
            throw new IllegalArgumentException("The number of users cannot be greater than 20.");
        }

        double numberOfDifferentGroups = Math.pow(2, allUsers.size());
        Set<GroupOfUsers> groupsSet = new HashSet<>((int) numberOfDifferentGroups);

        allUsers.stream().forEach(user -> groupsSet.add(new GroupOfUsers(user)));

        allUsers.stream().forEach(user -> {
            Set<GroupOfUsers> gruposAAñadir = new TreeSet<>(groupsSet);

            for (GroupOfUsers groupOfUsers : gruposAAñadir) {
                Set<User> members = new TreeSet<>(groupOfUsers.getMembers());

                members.add(user);
                groupsSet.add(new GroupOfUsers(members));

            }
        });

        return new ArrayList<>(groupsSet);
    }
}
