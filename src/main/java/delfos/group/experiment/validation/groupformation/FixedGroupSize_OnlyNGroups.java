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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

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
    public Collection<GroupOfUsers> generateGroups(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
        Random random = new Random(getSeedValue());
        LinkedList<GroupOfUsers> group = new LinkedList<>();
        int groupSizeValue = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);
        int numGroups = (Integer) getParameterValue(NUM_GROUPS_PARAMETER);

        final int maxNumGroups = datasetLoader.getRatingsDataset().allUsers().size() / groupSizeValue;

        if (maxNumGroups < numGroups) {
            throw new IllegalArgumentException("The number of groups * groupSize exceed the number of users (" + numGroups + " * " + groupSizeValue + " > " + datasetLoader.getRatingsDataset().allUsers().size());
        }

        ArrayList<Integer> usuarios;
        int numGrupos = datasetLoader.getRatingsDataset().allUsers().size() / groupSizeValue;
        usuarios = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());

        int indexGrupoActual = 0;
        while (group.size() < numGroups) {

            Set<User> usersGrupoActual = new TreeSet<>();
            while (usersGrupoActual.size() < groupSizeValue) {
                int idUser = usuarios.remove(random.nextInt(usuarios.size()));
                User user = datasetLoader.getUsersDataset().get(idUser);
                usersGrupoActual.add(user);

                if (usuarios.isEmpty()) {
                    usuarios.addAll(datasetLoader.getRatingsDataset().allUsers());
                }
            }
            group.add(new GroupOfUsers(usersGrupoActual));
            indexGrupoActual++;
            progressChanged("Group generation", indexGrupoActual / numGrupos);

            if (usuarios.isEmpty()) {
                usuarios.addAll(datasetLoader.getRatingsDataset().allUsers());
            }
        }

        while (group.size() > numGroups) {
            group.remove(random.nextInt(group.size()));
        }
        GroupOfUsers[] gruposGenerados = new GroupOfUsers[group.size()];

        indexGrupoActual = 0;
        for (GroupOfUsers grupoActual : group) {
            gruposGenerados[indexGrupoActual] = grupoActual;
            indexGrupoActual++;
        }

        progressChanged("Group generation", 100);
        return Arrays.asList(gruposGenerados);
    }
}
