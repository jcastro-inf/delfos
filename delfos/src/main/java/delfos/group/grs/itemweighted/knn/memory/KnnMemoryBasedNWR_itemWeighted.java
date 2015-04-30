package delfos.group.grs.itemweighted.knn.memory;

import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryModel;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class KnnMemoryBasedNWR_itemWeighted extends KnnCollaborativeRecommender<KnnMemoryModel> {

    private static final long serialVersionUID = 1L;

    public KnnMemoryBasedNWR_itemWeighted() {
        super();
        addParameter(NEIGHBORHOOD_SIZE);
        addParameter(SIMILARITY_MEASURE);
        addParameter(PREDICTION_TECHNIQUE);
        addParameter(INVERSE_FREQUENCY);
        addParameter(CASE_AMPLIFICATION);
        addParameter(DEFAULT_RATING);
        addParameter(DEFAULT_RATING_VALUE);
        addParameter(RELEVANCE_FACTOR);
        addParameter(RELEVANCE_FACTOR_VALUE);
    }

    public KnnMemoryBasedNWR_itemWeighted(
            CollaborativeSimilarityMeasure similarityMeasure,
            Integer relevanceFactor,
            Number defaultRating,
            boolean inverseFrequency,
            double caseAmplification,
            int neighborhoodSize,
            PredictionTechnique predictionTechnique) {

        this();

        setParameterValue(SIMILARITY_MEASURE, similarityMeasure);
        setParameterValue(RELEVANCE_FACTOR, relevanceFactor != null);
        setParameterValue(NEIGHBORHOOD_SIZE, neighborhoodSize);

        if (relevanceFactor != null && relevanceFactor <= 0) {
            throw new IllegalArgumentException("The relevance factor cannot be 0 or negative.");
        }
        if (relevanceFactor != null) {
            setParameterValue(RELEVANCE_FACTOR_VALUE, relevanceFactor);
        }
        setParameterValue(DEFAULT_RATING, defaultRating != null);
        if (defaultRating != null) {
            setParameterValue(DEFAULT_RATING_VALUE, defaultRating);
        }

        setParameterValue(INVERSE_FREQUENCY, inverseFrequency);
        setParameterValue(CASE_AMPLIFICATION, caseAmplification);
        setParameterValue(PREDICTION_TECHNIQUE, predictionTechnique);
    }

    public KnnMemoryBasedNWR_itemWeighted(
            CollaborativeSimilarityMeasure similarityMeasure,
            Integer relevanceFactor,
            int neighborhoodSize,
            PredictionTechnique predictionTechnique) {

        this();

        setParameterValue(SIMILARITY_MEASURE, similarityMeasure);
        setParameterValue(RELEVANCE_FACTOR, relevanceFactor != null);
        setParameterValue(NEIGHBORHOOD_SIZE, neighborhoodSize);

        if (relevanceFactor != null && relevanceFactor <= 0) {
            throw new IllegalArgumentException("The relevance factor cannot be 0 or negative.");
        }
        if (relevanceFactor != null) {
            setParameterValue(RELEVANCE_FACTOR_VALUE, relevanceFactor);
        }
        setParameterValue(PREDICTION_TECHNIQUE, predictionTechnique);
    }

    @Override
    public KnnMemoryModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new KnnMemoryModel();
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, KnnMemoryModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound {

        Map<Integer, Double> itemWeights = new TreeMap<>();

        Collection<Integer> userRated = datasetLoader.getRatingsDataset().getUserRated(idUser);

        userRated.stream().forEach((idItem) -> {
            itemWeights.put(idItem, 1.0 / userRated.size());
        });

        return recommendOnlyWithItemWeighting(datasetLoader, model, idUser, itemWeights, candidateItems);
    }

    public Collection<Recommendation> recommendOnlyWithItemWeighting(DatasetLoader<? extends Rating> datasetLoader,
            KnnMemoryModel model,
            Integer idUser,
            Map<Integer, Double> itemWeights,
            Collection<Integer> candidateItems) throws UserNotFound {
        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage(new Date().toGMTString() + " --> Recommending for user '" + idUser + "'\n");
        }

        try {
            List<Neighbor> neighbors;
            neighbors = getNeighbors(datasetLoader.getRatingsDataset(), idUser, itemWeights);

            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), idUser, neighbors, candidateItems);
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Finished recommendations for user '" + idUser + "'\n");
            }
            return ret;
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public List<Neighbor> getNeighbors(
            RatingsDataset<? extends Rating> ratingsDataset,
            int idUser,
            Map<Integer, Double> itemWeights)
            throws UserNotFound {

        ratingsDataset.getUserRated(idUser);

        List<KnnMemoryBasedNWR_itemWeighted_Task> tasks = new ArrayList<>();
        for (int idNeighbor : ratingsDataset.allUsers()) {
            try {
                tasks.add(new KnnMemoryBasedNWR_itemWeighted_Task(
                        ratingsDataset,
                        idUser, itemWeights,
                        idNeighbor,
                        this)
                );
            } catch (UserNotFound ex) {
            }
        }

        MultiThreadExecutionManager<KnnMemoryBasedNWR_itemWeighted_Task> multiThreadExecutionManager
                = new MultiThreadExecutionManager<>(
                        this.getName() + ":computeNeighborsOf" + idUser,
                        tasks,
                        KnnMemoryBasedNWR_itemWeighted_TaskExecutor.class);

        multiThreadExecutionManager.run();

        List<Neighbor> ret = Collections.synchronizedList(new ArrayList<>());
        multiThreadExecutionManager.getAllFinishedTasks().parallelStream().map((task) -> task.getNeighbor()).filter((neighbor) -> (neighbor != null)).forEach((neighbor) -> {
            ret.add(neighbor);
        });
        Collections.sort(ret);

        return ret;
    }

    public Collection<Recommendation> recommendWithNeighbors(
            RatingsDataset<? extends Rating> ratingsDataset,
            Integer idUser,
            List<Neighbor> vecinos,
            Collection<Integer> candidateItems)
            throws UserNotFound {

        PredictionTechnique predictionTechnique_ = (PredictionTechnique) getParameterValue(KnnMemoryBasedNWR_itemWeighted.PREDICTION_TECHNIQUE);

        //Predicción de la valoración
        Collection<Recommendation> recommendationList = new LinkedList<>();

        int numVecinos = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        for (int idItem : candidateItems) {
            Collection<MatchRating> match = new LinkedList<>();

            int numNeighborsUsed = 0;

            try {
                Map<Integer, ? extends Rating> itemRatingsRated = ratingsDataset.getItemRatingsRated(idItem);
                for (Neighbor ss : vecinos) {

                    Rating rating = itemRatingsRated.get(ss.getIdNeighbor());
                    if (rating != null) {
                        match.add(new MatchRating(RecommendationEntity.ITEM, ss.getIdNeighbor(), idItem, rating.ratingValue, ss.getSimilarity()));
                        numNeighborsUsed++;
                    }

                    if (numNeighborsUsed >= numVecinos) {
                        break;
                    }
                }

                try {
                    float predicted = predictionTechnique_.predictRating(idUser, idItem, match, ratingsDataset);
                    recommendationList.add(new Recommendation(idItem, predicted));

                } catch (CouldNotPredictRating ex) {
                }
            } catch (ItemNotFound ex) {
                Global.showError(ex);
            }
        }

        return recommendationList;
    }

    @Override
    public KnnMemoryModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return new KnnMemoryModel();
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, KnnMemoryModel model) throws FailureInPersistence {
        //No hay modelo que guardar.
    }
}
