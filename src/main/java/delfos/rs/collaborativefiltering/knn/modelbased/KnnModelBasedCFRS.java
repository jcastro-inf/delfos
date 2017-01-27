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
package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.utils.algorithm.progress.ProgressChangedController;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sistema de recomendación basado en el filtrado colaborativo basado en productos, también denominado Item-Item o
 * filtrado colaborativo basado en modelo. Este sistema de recomendación calcula un perfil de cada producto. El perfil
 * consta de los k vecinos más cercanos, es decir, los k ({@link KnnModelBasedCFRS#NEIGHBORHOOD_SIZE}) productos más
 * similares ({@link KnnModelBasedCFRS#SIMILARITY_MEASURE}). La predicción de la valoración de un producto i para un
 * usuario u se realiza agregando las valoraciones del usuario u sobre los productos vecinos del producto i, utilizando
 * para ello una técnica de predicción ({@link KnnModelBasedCFRS#PREDICTION_TECHNIQUE})
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class KnnModelBasedCFRS
        extends KnnCollaborativeRecommender<KnnModelBasedCFRSModel> {

    private static final long serialVersionUID = 1L;

    /**
     * Parámetro para almacenar el número de vecinos que se almacenan en el perfil de cada producto. Si no se modifica,
     * su valor por defecto es 20
     */
    public static final Parameter NEIGHBORHOOD_SIZE_STORE = new Parameter(
            "Neighborhood_size_store",
            new IntegerParameter(1, 9999, 1000));

    /**
     * Constructor por defecto que llama al constructor por defecto de la clase padre directa
     * {@link CollaborativeRecommender}. También asigna la suma ponderada como técnica de predicción a utilizar por
     * defecto
     */
    public KnnModelBasedCFRS() {
        super();
        addParameter(NEIGHBORHOOD_SIZE);
        addParameter(SIMILARITY_MEASURE);
        addParameter(PREDICTION_TECHNIQUE);
        addParameter(RELEVANCE_FACTOR);
        addParameter(RELEVANCE_FACTOR_VALUE);
        addParameter(NEIGHBORHOOD_SIZE_STORE);

        addParammeterListener(() -> {
            int neighborhoodSize = getNeighborhoodSize();
            int neighborhoodSizeStore = getNeighborhoodSizeStore();

            if (neighborhoodSizeStore < neighborhoodSize) {
                throw new IllegalArgumentException("The neighborhood size store must be greater than the neighborhood size.");
            }
        });
    }

    @Override
    public KnnModelBasedCFRSModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        CollaborativeSimilarityMeasure similarityMeasure = getSimilarityMeasure();
        Integer relevanceFactorValue = isRelevanceFactorApplied() ? getRelevanceFactorValue() : null;

        ProgressChangedController iknnModelProgress = new ProgressChangedController(
                getAlias() + " for dataset " + datasetLoader.getAlias(),
                datasetLoader.getContentDataset().size(),
                this::fireBuildingProgressChangedEvent
        );

        List<KnnModelItemProfile> allItemModels = datasetLoader.getContentDataset().parallelStream().map(item -> {
            List<Neighbor> thisItemNeighbors = getNeighbors(
                    datasetLoader,
                    item,
                    similarityMeasure,
                    relevanceFactorValue
            );

            thisItemNeighbors.sort(Neighbor.BY_SIMILARITY_DESC);

            thisItemNeighbors = thisItemNeighbors.stream()
                    .limit(getNeighborhoodSizeStore())
                    .collect(Collectors.toList());

            iknnModelProgress.setTaskFinished();

            return new KnnModelItemProfile(item.getId(), thisItemNeighbors);
        }).collect(Collectors.toList());

        Map<Integer, KnnModelItemProfile> itemModels_byItem = allItemModels.parallelStream().collect(Collectors.toMap(itemModel -> itemModel.getIdItem(), itemModel -> itemModel));

        return new KnnModelBasedCFRSModel(itemModels_byItem);
    }

    @Override
    public Collection<Recommendation> recommendToUser(
            DatasetLoader<? extends Rating> datasetLoader, KnnModelBasedCFRSModel model, Integer idUser, Set<Integer> candidateItems)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound {

        PredictionTechnique prediction = (PredictionTechnique) getParameterValue(KnnModelBasedCFRS.PREDICTION_TECHNIQUE);

        int neighborhoodSize = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        User user = datasetLoader.getUsersDataset().get(idUser);

        List<Recommendation> recommendations = Collections.synchronizedList(new LinkedList<>());
        Map<Integer, ? extends Rating> targetUserRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);

        for (int idItem : candidateItems) {
            List<MatchRating> matchRatings = new LinkedList<>();
            KnnModelItemProfile profile = model.getItemProfile(idItem);

            if (profile == null) {
                continue;
            }

            List<Neighbor> selectedNeighbors = profile.getAllNeighbors().stream()
                    .filter(neighbor -> neighbor.getSimilarity() > 0)
                    .filter(neighbor -> Double.isFinite(neighbor.getSimilarity()))
                    .filter(neighbor -> !Double.isNaN(neighbor.getSimilarity())).collect(Collectors.toList());

            selectedNeighbors = selectedNeighbors
                    .subList(0, Math.min(selectedNeighbors.size(), neighborhoodSize));

            for (Neighbor neighbor : selectedNeighbors) {
                double similarity = neighbor.getSimilarity();
                Rating rating = targetUserRatings.get(neighbor.getIdNeighbor());
                if (rating != null) {
                    Item itemNeighbor = datasetLoader.getContentDataset().get(rating.getIdItem());
                    matchRatings.add(new MatchRating(RecommendationEntity.USER, user, itemNeighbor, rating.getRatingValue().doubleValue(), similarity));
                }
            }

            Double predictedRating;
            try {
                predictedRating = prediction.predictRating(idUser, idItem, matchRatings, datasetLoader.getRatingsDataset());
            } catch (CouldNotPredictRating ex) {
                predictedRating = Double.NaN;
            }
            final Item item = datasetLoader.getContentDataset().get(idItem);
            recommendations.add(new Recommendation(item, predictedRating));

        }
        return recommendations;
    }

    /**
     * Computes the list of neighbors for the given item, sorted by similarity DESC.
     * <p>
     * <p>
     * This method must return a Neighbor object for each item in the dataset. For the neighbors that is not possible to
     * compute a similarity, {@link Double#NaN} similarity is assigned.
     * <p>
     * <p>
     * The neighbors will be selected in the prediction step.
     *
     * @param datasetLoader Data set used.
     * @param item1 Target item, for which the neighbors are computed.
     * @param similarityMeasure
     * @param relevanceFactorValue
     * @return A list wit a Neighbor object for each item in the dataset, sorted by similarity desc.
     */
    public static List<Neighbor> getNeighbors(
            DatasetLoader<? extends Rating> datasetLoader,
            Item item1,
            CollaborativeSimilarityMeasure similarityMeasure,
            Integer relevanceFactorValue
    ) {

        List<Neighbor> neighbors = datasetLoader.getContentDataset().parallelStream()
                .filter(item2 -> {
                    return !item1.equals(item2);
                })
                .map(item2 -> {
                    Collection<CommonRating> commonRatings = CommonRating.intersection(datasetLoader, item1, item2);

                    if (commonRatings.isEmpty()) {
                        return new Neighbor(RecommendationEntity.ITEM, item2, Double.NaN);
                    }

                    double similarity = similarityMeasure.similarity(commonRatings, datasetLoader.getRatingsDataset());

                    similarity = relevanceFactorValue != null ? similarity * Math.min(1.0, commonRatings.size() / relevanceFactorValue.doubleValue()) : similarity;

                    Neighbor neighbor = new Neighbor(RecommendationEntity.ITEM, item2, similarity);
                    return neighbor;
                })
                .collect(Collectors.toList());

        return neighbors;
    }

    @Override
    public KnnModelBasedCFRSModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items, DatasetLoader<? extends Rating> datasetLoader) throws FailureInPersistence {
        DAOKnnModelBasedDatabaseModel dao = new DAOKnnModelBasedDatabaseModel();
        return dao.loadModel(databasePersistence, users, items);
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, KnnModelBasedCFRSModel model) throws FailureInPersistence {
        DAOKnnModelBasedDatabaseModel dao = new DAOKnnModelBasedDatabaseModel();
        dao.saveModel(databasePersistence, model);
    }

    public final int getNeighborhoodSizeStore() {
        return (Integer) getParameterValue(NEIGHBORHOOD_SIZE_STORE);
    }
}
