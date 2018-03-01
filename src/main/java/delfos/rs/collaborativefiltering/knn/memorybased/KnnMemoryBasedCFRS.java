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
package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import delfos.rs.recommendation.RecommendationsToUserWithNeighbors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sistema de recomendación basado en el filtrado colaborativo basado en usuarios, también denominado User-User o
 * filtrado colaborativo basado en memoria. Este sistema de recomendación no realiza un cálculo de perfil de usuarios o
 * productos, sino que en el momento de la predicción, calcula los k vecinos más cercanos al usuario activo, es decir,
 * los k usuarios más similares. La predicción de la valoración de un producto i para un usuario u se
 * realiza agregando las valoraciones de los vecinos del usuario u sobre el producto i, utilizando para ello una técnica
 * de predicción.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class KnnMemoryBasedCFRS extends KnnCollaborativeRecommender<KnnMemoryModel> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que añade los parámetros al sistema de recomendación y asigna la medida del coseno y la suma
     * ponderada como medida de similitud y técnica de predicción respectivamente.
     */
    public KnnMemoryBasedCFRS() {
        super();
        addParameter(NEIGHBORHOOD_SIZE);
        addParameter(NEIGHBORHOOD_SIZE_STORE);
        addParameter(SIMILARITY_MEASURE);
        addParameter(PREDICTION_TECHNIQUE);
        addParameter(INVERSE_FREQUENCY);
        addParameter(CASE_AMPLIFICATION);
        addParameter(DEFAULT_RATING);
        addParameter(DEFAULT_RATING_VALUE);
        addParameter(RELEVANCE_FACTOR);
        addParameter(RELEVANCE_FACTOR_VALUE);

        addParammeterListener(() -> {
            int neighborhoodSize = getNeighborhoodSize();
            int neighborhoodSizeStore = getNeighborhoodSizeStore();

            if (neighborhoodSizeStore < neighborhoodSize) {
                throw new IllegalArgumentException("The neighborhood size store must be greater than the neighborhood size.");
            }
        });
    }

    @Override
    public <RatingType extends Rating> KnnMemoryModel buildRecommendationModel(
            DatasetLoader<RatingType> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new KnnMemoryModel();
    }

    @Override
    public <RatingType extends Rating> RecommendationsToUser recommendToUser(
            DatasetLoader<RatingType> datasetLoader,
            KnnMemoryModel model,
            User user, Set<Item> candidateItems
    ) throws UserNotFound {

        try {
            List<Neighbor> neighbors;
            neighbors = getNeighbors(datasetLoader, user, this);
            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), user, neighbors, candidateItems);
            return new RecommendationsToUserWithNeighbors(user, ret, neighbors);
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Calcula los vecinos mas cercanos del usuario indicado por parámetro. Para ello, utiliza los valores especificados
     * en los parámetros del algoritmo y los datasets de valoraciones y productos que se indicaron al sistema
     *
     * @param <RatingType>
     * @param datasetLoader Dataset de valoraciones.
     * @param user usuario para el que se calculan sus vecinos
     * @param rs
     * @return Devuelve una lista ordenada por similitud de los vecinos más cercanos al usuario indicado
     * @throws UserNotFound Si el usuario indicado no existe en el conjunto de datos
     */
    public static <RatingType extends Rating> List<Neighbor> getNeighbors(DatasetLoader<RatingType> datasetLoader, User user, KnnCollaborativeRecommender rs) throws UserNotFound {

        List<Neighbor> allNeighbors = datasetLoader.getUsersDataset().parallelStream()
                .filter(user2 -> !user.equals(user2))
                .map((userNeighbor) -> new KnnMemoryNeighborTask(datasetLoader, user, userNeighbor, rs))
                .map(new KnnMemoryNeighborCalculator())
                .map(neighbor -> {
                    if (neighbor == null) {
                        throw new IllegalStateException("Neighbor cannot be null!");
                    }
                    return neighbor;
                })
                .sorted(Neighbor.BY_SIMILARITY_DESC)
                .collect(Collectors.toList());

        if (Global.isVerboseAnnoying()) {
            Global.showMessageTimestamped("============ All users similarity =================");
            printNeighborhood(user.getId(), allNeighbors);
        }

        return allNeighbors;

    }

    /**
     * Devuelva las recomendaciones, teniendo en cuenta sólo los productos indicados por parámetro, para el usuario
     * activo a partir de los vecinos indicados por parámetro
     *
     * @param ratingsDataset Conjunto de valoraciones.
     * @param user Target user
     * @param neighbors Vecinos del usuario activo
     * @param candidateItems Lista de productos que se consideran recomendables, es decir, que podrían ser recomendados
     * si la predicción es alta
     * @return Lista de recomendaciones para el usuario, ordenadas por valoracion predicha.
     * @throws UserNotFound Si el usuario activo o alguno de los vecinos indicados no se encuentra en el dataset.
     */
    public <RatingType extends Rating> Collection<Recommendation> recommendWithNeighbors(
            RatingsDataset<RatingType> ratingsDataset,
            User user,
            List<Neighbor> neighbors,
            Collection<Item> candidateItems) {

        PredictionTechnique predictionTechnique_ = (PredictionTechnique) getParameterValue(KnnMemoryBasedCFRS.PREDICTION_TECHNIQUE);

        int neighborhoodSize_ = ((Number) getParameterValue(KnnMemoryBasedCFRS.NEIGHBORHOOD_SIZE)).intValue();

        List<Neighbor> neighborsWithPositiveSimilarityAndSelected = neighbors.parallelStream()
                .filter((neighbor -> Double.isFinite(neighbor.getSimilarity()) && neighbor.getSimilarity() > 0))
                .collect(Collectors.toList());
        neighborsWithPositiveSimilarityAndSelected.sort(Neighbor.BY_SIMILARITY_DESC);
        neighborsWithPositiveSimilarityAndSelected = neighborsWithPositiveSimilarityAndSelected
                .subList(0, Math.min(neighborsWithPositiveSimilarityAndSelected.size(), neighborhoodSize_));

        if (Global.isVerboseAnnoying()) {
            Global.showMessageTimestamped("============ Selected neighborhood =================");
            printNeighborhood(user.getId(), neighborsWithPositiveSimilarityAndSelected);
        }

        //Predicción de la valoración
        Collection<Recommendation> recommendations = new LinkedList<>();
        Map<Long, Map<Long, RatingType>> ratingsVecinos = neighborsWithPositiveSimilarityAndSelected
                .parallelStream()
                .collect(Collectors.toMap(
                        (neighbor -> neighbor.getIdNeighbor()),
                        (neighbor -> ratingsDataset.getUserRatingsRated(neighbor.getIdNeighbor()))));

        for (Item item : candidateItems) {
            Collection<MatchRating> match = new LinkedList<>();
            for (Neighbor neighbor : neighborsWithPositiveSimilarityAndSelected) {
                Rating rating = ratingsVecinos.get(neighbor.getIdNeighbor()).get(item.getId());
                if (rating != null) {
                    match.add(new MatchRating(RecommendationEntity.USER, neighbor.getIdNeighbor(), item.getId(), rating.getRatingValue(), neighbor.getSimilarity()));
                }
            }

            try {
                double predicted = predictionTechnique_.predictRating(user.getId(), item.getId(), match, ratingsDataset);
                recommendations.add(new Recommendation(item, predicted));
            } catch (CouldNotPredictRating ex) {
                recommendations.add(new Recommendation(item, Double.NaN));
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                recommendations.add(new Recommendation(item, Double.NaN));
            }
        }

        return recommendations;
    }

    @Override
    public <RatingType extends Rating> KnnMemoryModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Long> users, Collection<Long> items, DatasetLoader<RatingType> datasetLoader) throws FailureInPersistence {
        return new KnnMemoryModel();
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, KnnMemoryModel model) throws FailureInPersistence {
        //No hay modelo que guardar.
    }

    private static void printNeighborhood(long idUser, List<Neighbor> ret) {
        StringBuilder message = new StringBuilder();

        message.append("=========================================================\n");
        message.append("Neighbors of user '").append(idUser).append("'\n");
        for (Neighbor nei : ret) {
            message.append("\tnei: '").append(nei.getIdNeighbor());

            message.append("'\t\t\t");
            message.append(nei.getSimilarity()).append("\n");
        }
        message.append("=========================================================\n");

        Global.showMessageTimestamped(message.toString());
    }

    @Override
    public <RatingType extends Rating> Collection<Recommendation> recommendToUser(
            DatasetLoader<RatingType> datasetLoader,
            KnnMemoryModel model,
            long idUser,
            Set<Long> candidateItems) throws UserNotFound {

        PredictionTechnique predictionTechnique = (PredictionTechnique) getParameterValue(PREDICTION_TECHNIQUE);
        int neighborhoodSize = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        User user = datasetLoader.getUsersDataset().get(idUser);
        try {
            List<Neighbor> neighbors;
            neighbors = getNeighbors(datasetLoader, user, this);

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
     * Devuelva las recomendaciones, teniendo en cuenta sólo los productos indicados por parámetro, para el usuario
     * activo a partir de los vecinos indicados por parámetro
     *
     * @param datasetLoader Input data.
     * @param idUser Id del usuario activo
     * @param _neighborhood Vecinos del usuario activo
     * @param neighborhoodSize
     * @param candidateIdItems Lista de productos que se consideran recomendables, es decir, que podrían ser
     * recomendados si la predicción es alta
     * @param predictionTechnique
     * @return Lista de recomendaciones para el usuario, ordenadas por valoracion predicha.
     * @throws UserNotFound Si el usuario activo o alguno de los vecinos indicados no se encuentra en el dataset.
     */
    public static <RatingType extends Rating> Collection<Recommendation> recommendWithNeighbors(
            DatasetLoader<RatingType> datasetLoader,
            Long idUser,
            List<Neighbor> _neighborhood,
            int neighborhoodSize,
            Collection<Long> candidateIdItems,
            PredictionTechnique predictionTechnique)
            throws UserNotFound {

        List<Neighbor> neighborhood = _neighborhood.stream()
                .filter(neighbor -> !Double.isNaN(neighbor.getSimilarity()))
                .filter(neighbor -> neighbor.getSimilarity() > 0)
                .collect(Collectors.toList());

        neighborhood.sort(Neighbor.BY_SIMILARITY_DESC);

        RatingsDataset ratingsDataset = datasetLoader.getRatingsDataset();
        ContentDataset contentDataset = datasetLoader.getContentDataset();

        Collection<Recommendation> recommendationList = new ArrayList<>();

        List<Item> candidateItems = candidateIdItems.stream()
                .map(idItem -> contentDataset.get(idItem))
                .collect(Collectors.toList());

        for (Item item : candidateItems) {

            Collection<MatchRating> match = new ArrayList<>();
            int numNeighborsUsed = 0;
            try {
                Map<Long, RatingType> itemRatingsRated = ratingsDataset.getItemRatingsRated(item.getId());
                for (Neighbor neighbor : neighborhood) {

                    Rating rating = itemRatingsRated.get(neighbor.getIdNeighbor());
                    if (rating != null) {
                        match.add(new MatchRating(RecommendationEntity.ITEM, (User) neighbor.getNeighbor(), item, rating.getRatingValue(), neighbor.getSimilarity()));
                        numNeighborsUsed++;
                    }

                    if (numNeighborsUsed >= neighborhoodSize) {
                        break;
                    }
                }

                try {
                    double predicted = predictionTechnique.predictRating(idUser, item.getId(), match, ratingsDataset);
                    recommendationList.add(new Recommendation(item, predicted));

                } catch (CouldNotPredictRating ex) {
                }
            } catch (ItemNotFound ex) {
                Global.showError(ex);
            }
        }

        return recommendationList;
    }
}
