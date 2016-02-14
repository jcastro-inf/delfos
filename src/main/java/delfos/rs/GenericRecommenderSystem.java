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

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.ParameterOwner;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;
import java.util.Collection;

/**
 * Interfaz que implementa cualquier sistema de recomendación, ya sea de
 * recomendación a individuos, a grupos, etc.
 *
 * @param <RecommendationModel> Clase que almacena el modelo de recomendación
 * del sistema.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 08-Mar-2013
 * @version 2.0 26-Mayo-2013 Ahora los datasets se pasan por parámetro en cada
 * método.
 */
public interface GenericRecommenderSystem<RecommendationModel>
        extends ParameterOwner {

    /**
     * Añade un listener para que sea notificado del progreso de la construcción
     * del modelo del sistema de recomendación
     *
     * @param listener Objeto que desea ser notificado de los cambios
     */
    public void addRecommendationModelBuildingProgressListener(
            RecommendationModelBuildingProgressListener listener);

    /**
     * Elimina un listener para que no sea notificado más del progreso de la
     * construcción del modelo del sistema de recomendación
     *
     * @param listener Objeto que desea dejar de ser notificado de los cambios
     */
    public void removeRecommendationModelBuildingProgressListener(
            RecommendationModelBuildingProgressListener listener);

    /**
     * Esta función debe devolver true si el sistema de recomendación basa su
     * funcionamiento en la predicción de la valoración que el usuario daría al
     * item. Debe devolver false si se basa en la similitud del perfil con el
     * item.
     *
     * @return true si el sistema funciona mediante la predicción de ratings
     */
    public boolean isRatingPredictorRS();

    /**
     * Construcción del sistema de recomendación. En este método se debe
     * implementar la construcción del sistema de recomendación,
     *
     * @param datasetLoader Dataset que se utiliza para la construcción del
     * modelo devuelto.
     * @return Modelo de recomendación calculado a partir del dataset
     * especificado.
     */
    public RecommendationModel buildRecommendationModel(
            DatasetLoader<? extends Rating> datasetLoader)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset;

    public RecommendationModel loadRecommendationModel(
            FilePersistence filePersistence,
            Collection<Integer> users,
            Collection<Integer> items)
            throws FailureInPersistence;

    public RecommendationModel loadRecommendationModel(
            DatabasePersistence databasePersistence,
            Collection<Integer> users,
            Collection<Integer> items)
            throws FailureInPersistence;

    public void saveRecommendationModel(
            FilePersistence filePersistence,
            RecommendationModel model)
            throws FailureInPersistence;

    public void saveRecommendationModel(
            DatabasePersistence databasePersistence,
            RecommendationModel model)
            throws FailureInPersistence;
}
