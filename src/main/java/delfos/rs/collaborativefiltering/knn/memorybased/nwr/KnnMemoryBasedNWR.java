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
package delfos.rs.collaborativefiltering.knn.memorybased.nwr;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryModel;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryNeighborCalculator;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryNeighborTask;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sistema de recomendación basado en el filtrado colaborativo basado en usuarios, también denominado User-User o
 * filtrado colaborativo basado en memoria. Este sistema de recomendación no realiza un cálculo de perfil de usuarios o
 * productos, sino que en el momento de la predicción, calcula los k vecinos más cercanos al usuario activo, es decir,
 * los k ({@link KnnMemoryBasedCFRS#neighborhoodSize}) usuarios más similares
 * ({@link KnnMemoryBasedCFRS#similarityMeasure}). La predicción de la valoración de un producto i para un usuario u se
 * realiza agregando las valoraciones de los vecinos del usuario u sobre el producto i, utilizando para ello una técnica
 * de predicción ({@link KnnMemoryBasedCFRS#predictionTechnique})
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 27-02-2013
 */
public class KnnMemoryBasedNWR extends KnnMemoryBasedCFRS {

    private static final long serialVersionUID = 1L;

    public KnnMemoryBasedNWR() {
        super();
    }

    @Override
    public KnnMemoryModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) {
        return new KnnMemoryModel();
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, KnnMemoryModel model, Integer idUser, Set<Integer> candidateItems) throws UserNotFound {

        PredictionTechnique predictionTechnique = (PredictionTechnique) getParameterValue(PREDICTION_TECHNIQUE);
        int neighborhoodSize = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        try {
            List<Neighbor> neighbors;
            neighbors = getNeighbors(datasetLoader, idUser);

            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader, idUser, neighbors, neighborhoodSize, candidateItems, predictionTechnique);
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Finished recommendations for user '" + idUser + "'\n");
            }
            return ret;
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Calcula los vecinos mas cercanos del usuario indicado por parámetro. Para ello, utiliza los valores especificados
     * en los parámetros del algoritmo y los datasets de valoraciones y productos que se indicaron al sistema
     *
     * @param datasetLoader Dataset de valoraciones.
     * @param idUser id del usuario para el que se calculan sus vecinos
     * @return Devuelve una lista ordenada por similitud de los vecinos más cercanos al usuario indicado
     * @throws UserNotFound Si el usuario indicado no existe en el conjunto de datos
     */
    public List<Neighbor> getNeighbors(
            DatasetLoader<? extends Rating> datasetLoader,
            int idUser)
            throws UserNotFound {

        User user = datasetLoader.getUsersDataset().get(idUser);

        List<Neighbor> neigbors = datasetLoader.getUsersDataset().stream()
                .filter(neighbor -> !Objects.equals(neighbor.getId(), user.getId()))
                .map((neighbor) -> new KnnMemoryNeighborTask(datasetLoader, user, neighbor, this))
                .map(new KnnMemoryNeighborCalculator())
                .sorted(Neighbor.BY_SIMILARITY_DESC)
                .collect(Collectors.toList());

        return neigbors;
    }

    @Override
    public KnnMemoryModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items, DatasetLoader<? extends Rating> datasetLoader) throws FailureInPersistence {
        return new KnnMemoryModel();
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, KnnMemoryModel model) throws FailureInPersistence {
        //No hay modelo que guardar.
    }
}
