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
package delfos.rs;

import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Clase abstracta que define los métodos más generales de un sistema de recomendación, como el comportamiento que
 * soporta los listener de progreso de ejecución o los métodos set y get de los datasets
 *
 * @param <RecommendationModel> Clase que almacena el modelo de recomendación del sistema.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unkown date
 * @version 1.1 22-Jan-2013
 * @version 2.0 26-Mayo-2013 Ahora los datasets se pasan por parámetro en cada método.
 */
public abstract class GenericRecommenderSystemAdapter<RecommendationModel>
        extends ParameterOwnerAdapter
        implements GenericRecommenderSystem<RecommendationModel> {

    /**
     * Lista de objetos que desean ser notificados del cambio en el progreso de construcción del modelo de este sistema
     * de recomendación.
     */
    private final List<RecommendationModelBuildingProgressListener> progressListeners = Collections.synchronizedList(new LinkedList<RecommendationModelBuildingProgressListener>());

    /**
     * Añade un listener para que sea notificado del progreso de la construcción del modelo del sistema de recomendación
     *
     * @param listener Objeto que desea ser notificado de los cambios
     */
    @Override
    public void addRecommendationModelBuildingProgressListener(RecommendationModelBuildingProgressListener listener) {
        this.progressListeners.add(listener);
    }

    /**
     * Elimina un listener para que no sea notificado más del progreso de la construcción del modelo del sistema de
     * recomendación
     *
     * @param rl Objeto que desea dejar de ser notificado de los cambios
     */
    @Override
    public void removeRecommendationModelBuildingProgressListener(RecommendationModelBuildingProgressListener rl) {
        this.progressListeners.remove(rl);
    }

    /**
     * Notifica a todos los observadores del progreso de construcción del modelo
     * {@link RecommendationModelBuildingProgressListener} de un cambio en el progreso de construcción del mismo.
     *
     * @param actualJob Nombre de la tarea actual
     * @param percent Procentaje completado de la tarea actual
     * @param remainingSeconds Tiempo restante en esta tarea
     */
    protected void fireBuildingProgressChangedEvent(String actualJob, int percent, long remainingSeconds) {
        synchronized (progressListeners) {
            progressListeners.stream().forEach((listener) -> {
                listener.buildingProgressChanged(actualJob, percent, remainingSeconds);
            });
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecommendationModel loadRecommendationModel(FilePersistence filePersistence, Collection<Long> users, Collection<Long> items) throws FailureInPersistence {
        String completeFileName = filePersistence.getCompleteFileName();
        RecommendationModel model;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(completeFileName))) {
            model = (RecommendationModel) ois.readObject();
            if (model == null) {
                Global.showWarning("The loaded model is null. (Recommender: " + this.getClass().getName() + ")");
                throw new FailureInPersistence("The loaded model is null.");
            }

            return model;
        } catch (NotSerializableException ex) {
            Global.showWarning("The system " + this.getClass() + " has a model not serializable.");
            throw new UnsupportedOperationException(ex);
        } catch (Throwable ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public void saveRecommendationModel(FilePersistence filePersistence, RecommendationModel model) throws FailureInPersistence {

        File outputFile = new File(filePersistence.getCompleteFileName());

        if (FileUtilities.createDirectoriesForFileIfNotExist(outputFile)) {
            Global.showWarning("Created directory path " + outputFile.getAbsoluteFile().getParentFile() + " for recommendation model");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            oos.writeObject(model);
        } catch (NotSerializableException ex) {
            Global.showWarning("The system " + this.getClass() + " has a model not serializable.");
            throw new UnsupportedOperationException(ex);
        } catch (Throwable ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, RecommendationModel model) throws FailureInPersistence {
        throw new UnsupportedOperationException("The system " + this.getClass() + " does not implement the database persistence: this method should be overrided and perform the model saving.");
    }

    @Override
    public <RatingType extends Rating> RecommendationModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Long> users, Collection<Long> items, DatasetLoader<RatingType> datasetLoader) throws FailureInPersistence {
        throw new UnsupportedOperationException("The system " + this.getClass() + " does not implement the database persistence: this method should be overrided and perform the model loading.");
    }
}
