package delfos.rs.collaborativefiltering.knn.modelbased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;

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
    public KnnModelBasedCFRSModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        Collection<Integer> allRatedItems = ratingsDataset.allRatedItems();

        List<KnnModelBasedCBRS_Task> tasks = new ArrayList<>(allRatedItems.size());
        for (int idItem : allRatedItems) {
            tasks.add(new KnnModelBasedCBRS_Task(idItem, this, ratingsDataset));
        }

        MultiThreadExecutionManager<KnnModelBasedCBRS_Task> executionManager = new MultiThreadExecutionManager<>(
                "Profile creation",
                tasks,
                KnnModelBasedCBRS_TaskExecutor.class);

        executionManager.addExecutionProgressListener(this::fireBuildingProgressChangedEvent);
        executionManager.run();
        Map<Integer, KnnModelItemProfile> itemsProfiles = new TreeMap<>();
        for (KnnModelBasedCBRS_Task finishedTasks : executionManager.getAllFinishedTasks()) {
            List<Neighbor> neighbors = finishedTasks.getNeighbors();
            itemsProfiles.put(finishedTasks.idItem, new KnnModelItemProfile(finishedTasks.getIdItem(), neighbors));
        }

        fireBuildingProgressChangedEvent("Finished profile creation", 100, -1);
        return new KnnModelBasedCFRSModel(itemsProfiles);

    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, KnnModelBasedCFRSModel model, Integer idUser, java.util.Set<Integer> idItemList)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound {

        PredictionTechnique prediction = (PredictionTechnique) getParameterValue(KnnModelBasedCFRS.PREDICTION_TECHNIQUE);

        List<Recommendation> recommendationList = new LinkedList<>();
        Map<Integer, ? extends Rating> userRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);

        for (int idItem : idItemList) {
            List<MatchRating> matchRatings = new LinkedList<>();
            KnnModelItemProfile profile = model.getItemProfile(idItem);

            if (profile == null) {
                continue;
            }

            for (Neighbor neighbor : profile.getAllNeighbors()) {
                int idItemNeighbor = neighbor.getIdNeighbor();
                float similarity = neighbor.getSimilarity();
                Rating rating = userRated.get(idItemNeighbor);
                if (rating != null) {
                    matchRatings.add(new MatchRating(RecommendationEntity.USER, idUser, idItemNeighbor, rating.ratingValue, similarity));
                    if (Global.isVerboseAnnoying()) {
                        Global.showMessage(idItemNeighbor + "\the prediction is\t" + rating + "\n");
                    }
                }
            }

            if (!matchRatings.isEmpty()) {
                Float predictedRating;
                try {
                    predictedRating = prediction.predictRating(idUser, idItem, matchRatings, datasetLoader.getRatingsDataset());
                    recommendationList.add(new Recommendation(idItem, predictedRating));
                } catch (UserNotFound | ItemNotFound ex) {
                    throw new IllegalArgumentException(ex);
                } catch (CouldNotPredictRating ex) {
                }
            }

        }
        Collections.sort(recommendationList);
        return recommendationList;
    }

    /**
     * Obtiene el perfil de un producto dado. En Knn-ItemItem, el perfil de un
     * producto se compone de k productos similares al producto activo.
     *
     * @param ratingsDataset
     * @param idItem Producto para el que se calculan sus vecinos.
     * @return Lista de productos vecinos, ordenada por similitud con el
     * producto activo.
     * @throws ItemNotFound Cuando se intentan buscar los vecinos de un producto
     * que no existe en el dataset de valoraciones.
     */
    public List<Neighbor> getNeighbors(RatingsDataset<? extends Rating> ratingsDataset, int idItem) throws ItemNotFound {
        CollaborativeSimilarityMeasure similarityMeasureValue = (CollaborativeSimilarityMeasure) getParameterValue(SIMILARITY_MEASURE);
        int neighborhoodSizeValue = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        boolean isRelevanceFactorApplied = (Boolean) getParameterValue(RELEVANCE_FACTOR);
        int relevanceFactorIntValue = (Integer) getParameterValue(RELEVANCE_FACTOR_VALUE);

        List<Neighbor> itemsSimilares = new ArrayList<>();
        Map<Integer, ? extends Rating> itemRatingsRated = ratingsDataset.getItemRatingsRated(idItem);

        Collection<Integer> allItems = ratingsDataset.allRatedItems();
        for (int idItemNeighbor : allItems) {
            if (idItem != idItemNeighbor) {
                try {
                    Map<Integer, ? extends Rating> neighborRated = ratingsDataset.getItemRatingsRated(idItemNeighbor);
                    Set<Integer> intersection = new HashSet<>(itemRatingsRated.keySet());
                    intersection.retainAll(neighborRated.keySet());
                    if (intersection.isEmpty()) {
                        continue;
                    }
                    List<CommonRating> common = new LinkedList<>();
                    for (int idUser : intersection) {
                        common.add(new CommonRating(
                                RecommendationEntity.USER,
                                idUser,
                                RecommendationEntity.ITEM,
                                idItem,
                                idItemNeighbor,
                                itemRatingsRated.get(idUser).ratingValue.floatValue(),
                                neighborRated.get(idUser).ratingValue.floatValue()));
                    }

                    float similarity;
                    try {
                        similarity = similarityMeasureValue.similarity(common, ratingsDataset);

                        if (similarity > 0) {
                            if (isRelevanceFactorApplied && common.size() < relevanceFactorIntValue) {
                                similarity = (similarity * (common.size() / (float) relevanceFactorIntValue));
                            }
                            itemsSimilares.add(new Neighbor(RecommendationEntity.ITEM, idItemNeighbor, similarity));
                        }
                    } catch (CouldNotComputeSimilarity ex) {
                    }
                } catch (ItemNotFound ex) {
                    Global.showWarning("Item " + idItemNeighbor + " has no ratings.");
                }
            }
        }

        //Ordenar la lista y extraer los n mas similares
        Collections.sort(itemsSimilares);
        return itemsSimilares.subList(0, Math.min(itemsSimilares.size(), neighborhoodSizeValue));
    }

    @Override
    public KnnModelBasedCFRSModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        DAOKnnModelBasedDatabaseModel dao = new DAOKnnModelBasedDatabaseModel();
        return dao.loadModel(databasePersistence, users, items);
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, KnnModelBasedCFRSModel model) throws FailureInPersistence {
        DAOKnnModelBasedDatabaseModel dao = new DAOKnnModelBasedDatabaseModel();
        dao.saveModel(databasePersistence, model);
    }
}
