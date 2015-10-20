package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Sistema de recomendación basado en el filtrado colaborativo basado en
 * productos, también denominado Item-Item o filtrado colaborativo basado en
 * modelo. Este sistema de recomendación calcula un perfil de cada producto. El
 * perfil consta de los k vecinos más cercanos, es decir, los k
 * ({@link KnnModelBasedCFRS#NEIGHBORHOOD_SIZE}) productos más similares
 * ({@link KnnModelBasedCFRS#SIMILARITY_MEASURE}). La predicción de la
 * valoración de un producto i para un usuario u se realiza agregando las
 * valoraciones del usuario u sobre los productos vecinos del producto i,
 * utilizando para ello una técnica de predicción
 * ({@link KnnModelBasedCFRS#PREDICTION_TECHNIQUE})
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 28-02-2013
 */
public class KnnModelBasedCFRS
        extends KnnCollaborativeRecommender<KnnModelBasedCFRSModel> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que llama al constructor por defecto de la clase
     * padre directa {@link CollaborativeRecommender}. También asigna la suma
     * ponderada como técnica de predicción a utilizar por defecto
     */
    public KnnModelBasedCFRS() {
        super();
        addParameter(NEIGHBORHOOD_SIZE);
        addParameter(SIMILARITY_MEASURE);
        addParameter(PREDICTION_TECHNIQUE);
        addParameter(RELEVANCE_FACTOR);
        addParameter(RELEVANCE_FACTOR_VALUE);
    }

    @Override
    public KnnModelBasedCFRSModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        List<KnnModelBasedCBRS_Task> tasks = new ArrayList<>(datasetLoader.getContentDataset().size());
        for (Item item : datasetLoader.getContentDataset()) {
            tasks.add(new KnnModelBasedCBRS_Task(item, this, datasetLoader));
        }

        MultiThreadExecutionManager<KnnModelBasedCBRS_Task> executionManager = new MultiThreadExecutionManager<>(
                "Item-item model calculation",
                tasks,
                KnnModelBasedCBRS_TaskExecutor.class);

        executionManager.addExecutionProgressListener(this::fireBuildingProgressChangedEvent);
        executionManager.run();
        Map<Integer, KnnModelItemProfile> itemsProfiles = new TreeMap<>();
        for (KnnModelBasedCBRS_Task finishedTasks : executionManager.getAllFinishedTasks()) {
            List<Neighbor> neighbors = finishedTasks.getNeighbors();
            itemsProfiles.put(finishedTasks.item.getId(), new KnnModelItemProfile(finishedTasks.getItem().getId(), neighbors));
        }

        fireBuildingProgressChangedEvent("Finished item-item model calculation", 100, -1);
        return new KnnModelBasedCFRSModel(itemsProfiles);

    }

    @Override
    public Collection<Recommendation> recommendToUser(
            DatasetLoader<? extends Rating> datasetLoader, KnnModelBasedCFRSModel model, Integer idUser, Set<Integer> candidateItems)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound {

        PredictionTechnique prediction = (PredictionTechnique) getParameterValue(KnnModelBasedCFRS.PREDICTION_TECHNIQUE);

        int neighborhoodSize = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        List<Recommendation> recommendationList = Collections.synchronizedList(new LinkedList<>());
        Map<Integer, ? extends Rating> targetUserRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);

        for (int idItem : candidateItems) {
            List<MatchRating> matchRatings = new LinkedList<>();
            KnnModelItemProfile profile = model.getItemProfile(idItem);

            if (profile == null) {
                continue;
            }

            List<Neighbor> selectedNeighbors = profile.getAllNeighbors().stream()
                    .filter(neighbor -> neighbor.getSimilarity() > 0)
                    .filter(neighbor -> Float.isFinite(neighbor.getSimilarity()))
                    .filter(neighbor -> !Float.isNaN(neighbor.getSimilarity())).collect(Collectors.toList());

            selectedNeighbors = selectedNeighbors
                    .subList(0, Math.min(selectedNeighbors.size(), neighborhoodSize));

            for (Neighbor itemNeighbor : selectedNeighbors) {
                float similarity = itemNeighbor.getSimilarity();
                Rating rating = targetUserRatings.get(itemNeighbor.getIdNeighbor());
                if (rating != null) {
                    matchRatings.add(new MatchRating(RecommendationEntity.USER, idUser, itemNeighbor.getIdNeighbor(), rating.ratingValue, similarity));
                }
            }

            Float predictedRating;
            try {
                predictedRating = prediction.predictRating(idUser, idItem, matchRatings, datasetLoader.getRatingsDataset());
            } catch (CouldNotPredictRating ex) {
                predictedRating = Float.NaN;
            }

            recommendationList.add(new Recommendation(idItem, predictedRating));

        }
        return recommendationList;
    }

    /**
     * Computes the list of neighbors for the given item, sorted by similarity
     * DESC.
     * <p>
     * <p>
     * This method must return a Neighbor object for each item in the dataset.
     * For the neighbors that is not possible to compute a similarity,
     * {@link Float#NaN} similarity is assigned.
     * <p>
     * <p>
     * The neighbors will be selected in the prediction step.
     *
     * @param datasetLoader Data set used.
     * @param item Target item, for which the neighbors are computed.
     * @return A list wit a Neighbor object for each item in the dataset, sorted
     * by similarity desc.
     */
    public List<Neighbor> getNeighbors(DatasetLoader<? extends Rating> datasetLoader, Item item) {
        CollaborativeSimilarityMeasure similarityMeasureValue = (CollaborativeSimilarityMeasure) getParameterValue(SIMILARITY_MEASURE);

        boolean isRelevanceFactorApplied = (Boolean) getParameterValue(RELEVANCE_FACTOR);
        int relevanceFactorIntValue = (Integer) getParameterValue(RELEVANCE_FACTOR_VALUE);

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        ContentDataset contentDataset = ((ContentDatasetLoader) datasetLoader).getContentDataset();

        List<Neighbor> itemsSimilares = new ArrayList<>();
        Map<Integer, ? extends Rating> usersRatingsToTargetItem = ratingsDataset
                .getItemRatingsRated(item.getId());

        Set<Item> possibleNeighbors = contentDataset.parallelStream()
                .filter((item2 -> !item.equals(item2)))
                .collect(Collectors.toCollection(TreeSet::new));

        for (Item itemNeighbor : possibleNeighbors) {
            Map<Integer, ? extends Rating> usersRatingsToNeighborItem = ratingsDataset.getItemRatingsRated(itemNeighbor.getId());
            Set<Integer> intersection = new HashSet<>(usersRatingsToTargetItem.keySet());

            intersection.retainAll(usersRatingsToNeighborItem.keySet());

            List<CommonRating> common = intersection.stream()
                    .map((idUser -> new CommonRating(
                                    RecommendationEntity.USER,
                                    idUser,
                                    RecommendationEntity.ITEM,
                                    item.getId(),
                                    itemNeighbor.getId(),
                                    usersRatingsToTargetItem.get(idUser).ratingValue.floatValue(),
                                    usersRatingsToNeighborItem.get(idUser).ratingValue.floatValue())))
                    .collect(Collectors.toList());

            float similarity;
            try {
                similarity = similarityMeasureValue.similarity(common, ratingsDataset);
                if (similarity > 0
                        && isRelevanceFactorApplied
                        && common.size() < relevanceFactorIntValue) {
                    similarity = (similarity * (common.size() / (float) relevanceFactorIntValue));
                }
            } catch (CouldNotComputeSimilarity ex) {
                similarity = Float.NaN;
            }
            itemsSimilares.add(new Neighbor(RecommendationEntity.ITEM, itemNeighbor, similarity));

        }
        itemsSimilares.sort(Neighbor.BY_SIMILARITY_DESC);

        return itemsSimilares;
    }

    @Override
    public KnnModelBasedCFRSModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        DAOKnnModelBasedDatabaseModel dao = new DAOKnnModelBasedDatabaseModel();
        return dao.loadModel(databasePersistence, users, items);
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, KnnModelBasedCFRSModel model) throws FailureInPersistence {
        DAOKnnModelBasedDatabaseModel dao = new DAOKnnModelBasedDatabaseModel();
        dao.saveModel(databasePersistence, model);
    }
}
