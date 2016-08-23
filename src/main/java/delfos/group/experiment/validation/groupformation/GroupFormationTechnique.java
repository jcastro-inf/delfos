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
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.experiment.SeedHolder;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Clase abstracta que define los métodos que se utilizan para generar los grupos de usuarios que se utilizarán para
 * evaluar los sistemas de recomendación a grupos
 *
 * NOTA: Cuando se implementa una clase que herede de {@link GroupFormationTechnique}, se debe llamar en todos los
 * constructores que se implementen al método super(), para realizar las inicializaciones pertinentes.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class GroupFormationTechnique extends ParameterOwnerAdapter implements SeedHolder, Cloneable {

    /**
     * Añade los parámetros de la técnica de formación de grupos y realiza la inicialización de los valores aleatorios
     * que usa en la misma.
     */
    public GroupFormationTechnique() {
        addParameter(SEED);

        init();
    }

    /**
     * Realiza las inicializaciones pertinentes de la clase.
     */
    private void init() {
        addParammeterListener(new ParameterListener() {
            private long valorAnterior = (Long) getParameterValue(SEED);

            @Override
            public void parameterChanged() {
                long newValue = (Long) getParameterValue(SEED);
                if (valorAnterior != newValue) {
                    valorAnterior = newValue;
                    setSeedValue(newValue);
                }
            }
        });
    }

    /**
     * Método para generar los grupos de usuarios que se deben evaluar. Los grupos de usuarios
     *
     * @param datasetLoader
     * @return
     * @see GroupFormationTechnique#iterator()
     */
    public Collection<GroupOfUsers> generateGroups(DatasetLoader<? extends Rating> datasetLoader) {
        return generateGroups(datasetLoader, datasetLoader.getUsersDataset().stream().collect(Collectors.toList()));
    }

    /**
     * Método para generar los grupos de usuarios que se deben evaluar. Los grupos de usuarios
     *
     * @param datasetLoader
     * @param usersAllowed
     * @return
     * @see GroupFormationTechnique#iterator()
     */
    public abstract Collection<GroupOfUsers> generateGroups(DatasetLoader<? extends Rating> datasetLoader, Collection<User> usersAllowed) throws CannotLoadRatingsDataset;

    private List<GroupFormationTechniqueProgressListener> listeners = Collections.synchronizedList(new LinkedList<>());

    public void addListener(GroupFormationTechniqueProgressListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GroupFormationTechniqueProgressListener listener) {
        listeners.remove(listener);
    }

    protected void progressChanged(String message, int progressPercent) {
        progressChanged(message, progressPercent, -1);
    }

    protected void progressChanged(String message, int progressPercent, long remainingTimeInMS) {
        synchronized (listeners) {
            listeners.stream().forEach((listener) -> {
                listener.progressChanged(message, progressPercent, remainingTimeInMS);
            });
        }
    }

    @Override
    public ParameterOwner clone() {
        GroupFormationTechnique clone = (GroupFormationTechnique) super.clone();
        clone.listeners = Collections.synchronizedList(new ArrayList<>());
        return clone;
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return ((Number) getParameterValue(SEED)).longValue();
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_FORMATION_TECHNIQUE;
    }

    public int getGroupSize() {
        return (Integer) getParameterValue(FixedGroupSize_OnlyNGroups.GROUP_SIZE_PARAMETER);
    }

    protected static void checkUsersAllowedAreInDatasetLoader(DatasetLoader<? extends Rating> datasetLoader, Collection<User> usersAllowed) {
        Optional<User> userNotInDataset = usersAllowed.parallelStream().filter(user -> {
            try {
                User get = datasetLoader.getUsersDataset().get(user.getId());
                return get == null;
            } catch (UserNotFound ex) {
                return true;
            }

        }).findFirst();

        if (userNotInDataset.isPresent()) {
            throw new IllegalArgumentException("User " + userNotInDataset.get() + " is not in the dataset");
        }
    }

    public static void checkDatasetIsNotNull(DatasetLoader<? extends Rating> datasetLoader) throws IllegalStateException {
        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
    }
}
