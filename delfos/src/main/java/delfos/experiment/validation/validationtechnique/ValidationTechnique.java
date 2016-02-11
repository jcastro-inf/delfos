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
package delfos.experiment.validation.validationtechnique;

import java.util.LinkedList;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.experiment.SeedHolder;

/**
 * Clase abstracta que especifica los métodos que deberá tener una técnica de
 * validación. Todas las técnicas de validación deben usar la variable random en
 * sus consultas para obtener valores aleatorios. De esta manera se garantiza
 * que distintos algoritmos utilicen la misma muestra aleatoria.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (19 Octubre 2011)
 */
public abstract class ValidationTechnique extends ParameterOwnerAdapter implements SeedHolder {

    //--------------- Métodos para garantizar los valores aleatorios -----------
    /**
     * Constructor por defecto que agrega el parámetro para la semilla e
     * inicializa la variable random. Además, añade el listener para mantener el
     * valor de {@link ValidationTechnique#random} actualizado.
     */
    public ValidationTechnique() {
        super();
        this.listeners = new LinkedList<>();

        init();
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }
    /**
     * Lista de objetos que han solicitado que se notifique del progreso de
     * ejecución de esta técnica de evaluación
     */
    private final LinkedList<ValidationTechniqueProgressListener> listeners;

    /**
     * Añade un listener a la técnica de validación.
     *
     * @param listener Observador a añadir.
     */
    public void addListener(ValidationTechniqueProgressListener listener) {
        listeners.add(listener);
        listener.progressChanged("", 0);
    }

    /**
     * Elimina un listener a la técnica de validación.
     *
     * @param listener Observador a eliminar.
     */
    public void removeListener(ValidationTechniqueProgressListener listener) {
        listeners.remove(listener);
    }

    /**
     * Lanza el evento de progreso cambiado, para indicar a todos los
     * observadores registrados del cambio.
     *
     * @param message Mensaje que indica la tarea actual.
     * @param percent Porcentaje completado.
     */
    protected void progressChanged(String message, int percent) {
        listeners.stream().forEach((listener) -> {
            listener.progressChanged(message, percent);
        });
    }

    /**
     * Esta función se debe implementar la generación de los conjuntos de
     * validación y será invocada cada vez que se realice una ejecución, para
     * probar conjuntos de validación diferentes
     *
     * @param datasetLoader Conjunto de datos iniciales.
     * @return Vector en el que cada elemento contiene un par training/test.
     */
    public abstract PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset;

    /**
     * Realiza las inicializaciones pertinentes.
     */
    private void init() {

        addParameter(SEED);
        addParammeterListener(new ParameterListener() {
            private long valorAnterior = (Long) getParameterValue(SEED);

            @Override
            public void parameterChanged() {
                long newValue = (Long) ValidationTechnique.this.getParameterValue(SEED);
                if (valorAnterior != newValue) {
                    if (Global.isVerboseAnnoying()) {
                        Global.showWarning("Reset " + ValidationTechnique.this.getName() + " to seed = " + newValue + "\n");
                    }
                    valorAnterior = newValue;
                    ValidationTechnique.this.setSeedValue(newValue);
                }
            }
        });
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.VALIDATION_TECHNIQUE;
    }

    /**
     * Numero de particiones que realiza. Será la longitud del vector devuelto
     * por el método {@link ValidationTechnique#shuffle(delfos.Dataset.Persistence.DatasetLoader)
     * }.
     *
     * @return
     */
    public abstract int getNumberOfSplits();
}
