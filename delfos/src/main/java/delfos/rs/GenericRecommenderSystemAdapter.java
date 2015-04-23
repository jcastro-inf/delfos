package delfos.rs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;

/**
 * Clase abstracta que define los métodos más generales de un sistema de
 * recomendación, como el comportamiento que soporta los listener de progreso de
 * ejecución o los métodos set y get de los datasets
 *
 * @param <RecommendationModel> Clase que almacena el modelo de recomendación
 * del sistema.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unkown date
 * @version 1.1 22-Jan-2013
 * @version 2.0 26-Mayo-2013 Ahora los datasets se pasan por parámetro en cada
 * método.
 */
public abstract class GenericRecommenderSystemAdapter<RecommendationModel> extends ParameterOwnerAdapter implements GenericRecommenderSystem<RecommendationModel> {

    /**
     * Lista de objetos que desean ser notificados del cambio en el progreso de
     * construcción del modelo de este sistema de recomendación.
     */
    private final List<RecommenderSystemBuildingProgressListener> progressListeners = Collections.synchronizedList(new LinkedList<RecommenderSystemBuildingProgressListener>());

    /**
     * Añade un listener para que sea notificado del progreso de la construcción
     * del modelo del sistema de recomendación
     *
     * @param listener Objeto que desea ser notificado de los cambios
     */
    @Override
    public void addBuildingProgressListener(RecommenderSystemBuildingProgressListener listener) {
        this.progressListeners.add(listener);
        listener.buildingProgressChanged("", 0, -1);
    }

    /**
     * Elimina un listener para que no sea notificado más del progreso de la
     * construcción del modelo del sistema de recomendación
     *
     * @param rl Objeto que desea dejar de ser notificado de los cambios
     */
    @Override
    public void removeBuildingProgressListener(RecommenderSystemBuildingProgressListener rl) {
        this.progressListeners.remove(rl);
    }

    /**
     * Notifica a todos los observadores del progreso de construcción del modelo
     * {@link RecommenderSystemBuildingProgressListener} de un cambio en el
     * progreso de construcción del mismo.
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
    public RecommendationModel loadModel(FilePersistence filePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
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
    public void saveModel(FilePersistence filePersistence, RecommendationModel model) throws FailureInPersistence {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePersistence.getCompleteFileName()))) {
            oos.writeObject(model);
        } catch (NotSerializableException ex) {
            Global.showWarning("The system " + this.getClass() + " has a model not serializable.");
            throw new UnsupportedOperationException(ex);
        } catch (FileNotFoundException ex) {
            File recommendationModelDirectory = filePersistence.getDirectory().getAbsoluteFile();
            Global.showWarning("Directory " + recommendationModelDirectory.getAbsolutePath() + " for recommendation model not exists");

            boolean mkdirs = recommendationModelDirectory.mkdirs();
            if (mkdirs) {
                Global.showWarning("Created directory " + recommendationModelDirectory.getAbsolutePath() + " for recommendation model");
                saveModel(filePersistence, model);
            } else {
                Global.showWarning("Cannot create directory " + recommendationModelDirectory.getAbsolutePath() + " for recommendation model");
                throw new FailureInPersistence(ex);
            }
        } catch (Throwable ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, RecommendationModel model) throws FailureInPersistence {
        throw new UnsupportedOperationException("The system " + this.getClass() + " does not implement the database persistence: this method should be overrided and perform the model saving.");
    }

    @Override
    public RecommendationModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        throw new UnsupportedOperationException("The system " + this.getClass() + " does not implement the database persistence: this method should be overrided and perform the model loading.");
    }
}
