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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Validación de grupos que genera grupos de usuarios, utilizando miembros
 * elegidos aleatoriamente, de un tamaño fijo.
 *
* @author Jorge Castro Gallardo
 */
public class FixedGroupSize extends GroupFormationTechnique {

    /**
     * Parámetro para establecer el número de usuarios que tendrán los grupos
     * generados con esta validación de grupos. Por defecto toma el valor 5.
     */
    public static final Parameter groupSize = new Parameter("groupSize", new IntegerParameter(1, 10000, 5));

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cinco miembros.
     */
    public FixedGroupSize() {
        super();
        addParameter(groupSize);
    }

    /**
     * Crea los grupos con el tamaño predefinido.
     *
     * @param groupSize
     */
    public FixedGroupSize(int groupSize) {
        this();
        setParameterValue(FixedGroupSize.groupSize, groupSize);
    }

    @Override
    public Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {

        final long seed = getSeedValue();
        Random random = new Random(seed);

        LinkedList<GroupOfUsers> grupos = new LinkedList<>();
        int groupSizeValue = (Integer) getParameterValue(groupSize);
        ArrayList<Integer> usuarios;
        int numGrupos = datasetLoader.getRatingsDataset().allUsers().size() / groupSizeValue;
        usuarios = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());

        int indexGrupoActual = 0;
        while (!usuarios.isEmpty() && usuarios.size() >= groupSizeValue) {

            Set<Integer> usersGrupoActual = new TreeSet<>();
            for (int i = 0; i < groupSizeValue; i++) {
                int idUser = usuarios.remove(random.nextInt(usuarios.size()));
                usersGrupoActual.add(idUser);
            }
            grupos.add(new GroupOfUsers(usersGrupoActual));
            indexGrupoActual++;
            progressChanged("Group generation", indexGrupoActual / numGrupos);
        }
        GroupOfUsers[] groupOfUsers = new GroupOfUsers[grupos.size()];

        indexGrupoActual = 0;
        for (GroupOfUsers grupoActual : grupos) {
            groupOfUsers[indexGrupoActual] = grupoActual;
            indexGrupoActual++;
        }

        progressChanged("Group generation", 100);

        List<GroupOfUsers> listOfGroupOfUsers = Arrays.asList(groupOfUsers);

        return listOfGroupOfUsers;
    }
}
