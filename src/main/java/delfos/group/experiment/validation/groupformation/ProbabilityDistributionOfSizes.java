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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Validación de grupos que genera grupos de usuarios, utilizando miembros
 * elegidos aleatoriamente, de un tamaño fijo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ProbabilityDistributionOfSizes extends GroupFormationTechnique {

    private double[] acumulateProbabilities = null;

    /**
     * Parámetro para establecer el número de usuarios que tendrán los grupos
     * generados con esta validación de grupos
     */
    public static final Parameter numGroups = new Parameter("numGroups", new IntegerParameter(1, 10000000, 5));

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     */
    public ProbabilityDistributionOfSizes() {
        super();
        addParameter(numGroups);
    }

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     *
     * @param numGroupsValue Número de grupos que se generan.
     * @param probabilitiesVector Probabilidad de que un grupo de tamaño i+2 se
     * genere.
     */
    public ProbabilityDistributionOfSizes(int numGroupsValue, double... probabilitiesVector) {
        super();
        addParameter(numGroups);
        setParameterValue(numGroups, numGroupsValue);

        double norma = 0;
        for (double x : probabilitiesVector) {
            norma += x;
        }

        acumulateProbabilities = new double[probabilitiesVector.length];

        acumulateProbabilities[0] = (double) (probabilitiesVector[0] / norma);
        double anterior = acumulateProbabilities[0];
        for (int i = 1; i < probabilitiesVector.length; i++) {
            acumulateProbabilities[i] = (double) (anterior + probabilitiesVector[i] / norma);
            anterior = acumulateProbabilities[i];
        }
    }

    @Override
    public Collection<GroupOfUsers> generateGroups(DatasetLoader<? extends Rating> datasetLoader, Collection<User> usersAllowed) throws CannotLoadRatingsDataset {
        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
        Random random = new Random(getSeedValue());

        int numGroupsValue = (Integer) getParameterValue(numGroups);

        Set<GroupOfUsers> grupos = new HashSet<>(numGroupsValue);

        ArrayList<User> usuarios;
        usuarios = new ArrayList<>(datasetLoader.getUsersDataset());

        int indexGrupoActual = 0;
        while (grupos.size() < numGroupsValue) {

            Set<User> usersGrupoActual = new TreeSet<>();

            int groupSize = getGroupSize(random.nextLong());
            while (usersGrupoActual.size() < groupSize) {
                User idUser = usuarios.remove(random.nextInt(usuarios.size()));
                usersGrupoActual.add(idUser);

                if (usuarios.isEmpty()) {
                    usuarios.addAll(datasetLoader.getUsersDataset());
                }
            }
            boolean add = grupos.add(new GroupOfUsers(usersGrupoActual));

            if (add && grupos.size() % 10000 == 0) {

                indexGrupoActual++;
            }

            if (usuarios.isEmpty()) {
                usuarios.addAll(datasetLoader.getUsersDataset());
            }
        }
        GroupOfUsers[] groupOfUsers = new GroupOfUsers[grupos.size()];

        indexGrupoActual = 0;
        for (GroupOfUsers grupoActual : grupos) {
            groupOfUsers[indexGrupoActual] = grupoActual;
            indexGrupoActual++;
        }

        progressChanged("Group generation", 100);

        return Arrays.asList(groupOfUsers);
    }

    private int getGroupSize(long seed) {
        double r = new Random(seed).nextDouble();
        int size = -1;

        for (int i = 0; i < acumulateProbabilities.length; i++) {
            if (acumulateProbabilities[i] > r) {
                size = i + 2;
                break;
            }
        }
        return size;
    }
}
