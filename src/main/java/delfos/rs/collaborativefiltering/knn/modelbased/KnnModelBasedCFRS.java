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

import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.utils.algorithm.progress.ProgressChangedController;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public <RatingType extends Rating> KnnModelBasedCFRSModel buildRecommendationModel(
            DatasetLoader<RatingType> datasetLoader
    ) throws CannotLoadRatingsDataset {

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

        Map<Long, KnnModelItemProfile> itemModels_byItem = allItemModels.parallelStream().collect(Collectors.toMap(itemModel -> itemModel.getIdItem(), itemModel -> itemModel));

        return new KnnModelBasedCFRSModel(itemModels_byItem);
    }

    @Override
    public <RatingType extends Rating> Collection<Recommendation> recommendToUser(
            DatasetLoader<RatingType> datasetLoader,
            KnnModelBasedCFRSModel model,
            long idUser,
            Set<Long> candidateItems)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound {

        PredictionTechnique prediction = (PredictionTechnique) getParameterValue(KnnModelBasedCFRS.PREDICTION_TECHNIQUE);

        
        Map<Long, RatingType> userRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
        if (userRated.isEmpty()) {
            return candidateItems.parallelStream().
                    map(idItem -> datasetLoader.getContentDataset().get(idItem)).
                    map(item -> new Recommendation(item, Double.NaN)).collect(Collectors.toList());
        }

        int neighborhoodSize = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        final ContentDataset contentDataset = datasetLoader.getContentDataset();
        
        Collection<Recommendation> recommendationList = candidateItems.parallelStream().
                map(idItem -> contentDataset.get(idItem)).
                map(item -> {
                    KnnModelItemProfile profile = model.getItemProfile(item.getId());
                            
                    if(profile == null){
                        return new Recommendation(item, Double.NaN);
                    }
                    
                    Collection<Neighbor> neighbors = profile.getAllNeighbors().stream()
                            .filter(neighbor -> !Double.isNaN(neighbor.getSimilarity()))
                            .filter(neighbor -> neighbor.getSimilarity()> 0)
                            .sorted(Neighbor.BY_SIMILARITY_DESC)
                            .limit(neighborhoodSize)
                            .collect(Collectors.toList());

                    List<MatchRating> matchRatings = neighbors.parallelStream().flatMap(neighbor->{
                        long idItemNeighbor = neighbor.getIdNeighbor();
                        double similarity = neighbor.getSimilarity();
                        Rating rating = userRated.get(idItemNeighbor);

                        if(rating==null) return (Stream<MatchRating>) Collections.EMPTY_LIST.stream();
                        else{
                            MatchRating matchRating = new MatchRating(RecommendationEntity.USER, idUser, idItemNeighbor, rating, similarity);
                            return (Stream<MatchRating>) Arrays.asList(matchRating).stream();
                        }
                    }).collect(Collectors.toList());
                    
            

                    Double predictedRating = Double.NaN;
                    if (!matchRatings.isEmpty()) {
                        try {
                            predictedRating = prediction.predictRating(idUser, item.getId(), matchRatings, datasetLoader.getRatingsDataset());

                        } catch (UserNotFound | ItemNotFound ex) {
                            throw new IllegalArgumentException(ex);
                        } catch (CouldNotPredictRating ex) {
                            
                        } 
                    }
                    
                    return new Recommendation(item, predictedRating);
                }).
                collect(Collectors.toList());
        
        return recommendationList;
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
    public static <RatingType extends Rating> List<Neighbor> getNeighbors(
            DatasetLoader<RatingType> datasetLoader,
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
    public <RatingType extends Rating> KnnModelBasedCFRSModel loadRecommendationModel(
            DatabasePersistence databasePersistence,
            Collection<Long> users,
            Collection<Long> items,
            DatasetLoader<RatingType> datasetLoader
    ) throws FailureInPersistence {

        DAOKnnModelBasedDatabaseModel dao = new DAOKnnModelBasedDatabaseModel();
        return dao.loadModel(databasePersistence, users, items);
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, KnnModelBasedCFRSModel model) throws FailureInPersistence {
        DAOKnnModelBasedDatabaseModel dao = new DAOKnnModelBasedDatabaseModel();
        dao.saveModel(databasePersistence, model);
    }

}
