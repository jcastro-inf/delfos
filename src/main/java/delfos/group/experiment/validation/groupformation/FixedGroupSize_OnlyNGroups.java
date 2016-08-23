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
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Validación de grupos que genera grupos de usuarios, utilizando miembros elegidos aleatoriamente, de un tamaño fijo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class FixedGroupSize_OnlyNGroups extends GroupFormationTechnique {

    /**
     * Parámetro para establecer el número de usuarios que tendrán los grupos generados con esta validación de grupos
     */
    public static final Parameter GROUP_SIZE_PARAMETER = new Parameter("groupSize", new IntegerParameter(1, 10000, 5));
    public static final Parameter NUM_GROUPS_PARAMETER = new Parameter("numGroups", new IntegerParameter(1, 1000000, 5));

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por defecto, el tamaño de los grupos es de
     * cuatro miembros.
     */
    public FixedGroupSize_OnlyNGroups() {
        super();
        addParameter(GROUP_SIZE_PARAMETER);
        addParameter(NUM_GROUPS_PARAMETER);
    }

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por defecto, el tamaño de los grupos es de
     * cuatro miembros.
     *
     * @param groupSizeValue Tamaño de los grupos generados
     * @param numGroupsValue Número de grupos considerados
     */
    public FixedGroupSize_OnlyNGroups(int numGroupsValue, int groupSizeValue) {
        this();

        setParameterValue(GROUP_SIZE_PARAMETER, groupSizeValue);
        setParameterValue(NUM_GROUPS_PARAMETER, numGroupsValue);

    }

    @Override
    public Collection<GroupOfUsers> generateGroups(
            DatasetLoader<? extends Rating> datasetLoader,
            Collection<User> usersAllowed) {

        int groupSizeValue = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);
        int numGroups = (Integer) getParameterValue(NUM_GROUPS_PARAMETER);

        checkDatasetIsNotNull(datasetLoader);
        checkUsersAllowedAreInDatasetLoader(datasetLoader, usersAllowed);
        checkNumGroupsIsValid(datasetLoader, groupSizeValue, numGroups);

        Random random = new Random(getSeedValue());
        List<GroupOfUsers> groupsGenerated = new ArrayList<>(numGroups);

        final List<Integer> usersAllowedSorted = usersAllowed.parallelStream()
                .map(user -> user.getId())
                .sorted()
                .collect(Collectors.toList());

        List<Integer> users = new ArrayList<>(usersAllowedSorted);

        int indexGrupoActual = 0;
        while (groupsGenerated.size() < numGroups) {

            Set<User> usersGrupoActual = new TreeSet<>();
            while (usersGrupoActual.size() < groupSizeValue) {
                int idUser = users.remove(random.nextInt(users.size()));
                User user = datasetLoader.getUsersDataset().get(idUser);
                usersGrupoActual.add(user);

                if (users.isEmpty()) {
                    users.addAll(usersAllowedSorted);
                }
            }
            groupsGenerated.add(new GroupOfUsers(usersGrupoActual));
            indexGrupoActual++;
            progressChanged("Group generation", indexGrupoActual / numGroups);
        }

        progressChanged("Group generation", 100);
        return groupsGenerated;
    }

    public void checkNumGroupsIsValid(DatasetLoader<? extends Rating> datasetLoader, int groupSizeValue, int numGroups) throws CannotLoadRatingsDataset, IllegalArgumentException {
        final int maxNumGroups = datasetLoader.getRatingsDataset().allUsers().size() / groupSizeValue;
        if (maxNumGroups < numGroups) {
            throw new IllegalArgumentException("The number of groups * groupSize exceed the number of users (" + numGroups + " * " + groupSizeValue + " > " + datasetLoader.getRatingsDataset().allUsers().size());
        }
    }

}
