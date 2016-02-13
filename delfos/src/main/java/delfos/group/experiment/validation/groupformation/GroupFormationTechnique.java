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
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.SeedHolder;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Clase abstracta que define los métodos que se utilizan para generar los
 * grupos de usuarios que se utilizarán para evaluar los sistemas de
 * recomendación a grupos
 *
 * NOTA: Cuando se implementa una clase que herede de
 * {@link GroupFormationTechnique}, se debe llamar en todos los constructores
 * que se implementen al método super(), para realizar las inicializaciones
 * pertinentes.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class GroupFormationTechnique extends ParameterOwnerAdapter implements SeedHolder {

    /**
     * Añade los parámetros de la técnica de formación de grupos y realiza la
     * inicialización de los valores aleatorios que usa en la misma.
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
     * Método para generar los grupos de usuarios que se deben evaluar. Los
     * grupos de usuarios
     *
     * @param datasetLoader
     * @return
     * @see GroupFormationTechnique#iterator()
     */
    public abstract Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset;

    private final LinkedList<GroupFormationTechniqueProgressListener> listeners = new LinkedList<>();

    public void addListener(GroupFormationTechniqueProgressListener listener) {
        listeners.add(listener);
        listener.progressChanged("", 0, -1);
    }

    public void removeListener(GroupFormationTechniqueProgressListener listener) {
        listeners.remove(listener);
    }

    protected void progressChanged(String message, int progressPercent) {
        progressChanged(message, progressPercent, -1);
    }

    protected void progressChanged(String message, int progressPercent, long remainingTimeInMS) {
        listeners.stream().forEach((listener) -> {
            listener.progressChanged(message, progressPercent, remainingTimeInMS);
        });
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

}
