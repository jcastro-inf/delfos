package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsWithNeighbors;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sistema de recomendación basado en el filtrado colaborativo basado en
 * usuarios, también denominado User-User o filtrado colaborativo basado en
 * memoria. Este sistema de recomendación no realiza un cálculo de perfil de
 * usuarios o productos, sino que en el momento de la predicción, calcula los k
 * vecinos más cercanos al usuario activo, es decir, los k
 * ({@link KnnMemoryBasedCFRS#neighborhoodSize}) usuarios más similares
 * ({@link KnnMemoryBasedCFRS#similarityMeasure}). La predicción de la
 * valoración de un producto i para un usuario u se realiza agregando las
 * valoraciones de los vecinos del usuario u sobre el producto i, utilizando
 * para ello una técnica de predicción
 * ({@link KnnMemoryBasedCFRS#predictionTechnique})
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 27-02-2013
 */
public class KnnMemoryBasedCFRS extends KnnCollaborativeRecommender<KnnMemoryModel> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que añade los parámetros al sistema de recomendación y asigna
     * la medida del coseno y la suma ponderada como medida de similitud y
     * técnica de predicción respectivamente.
     */
    public KnnMemoryBasedCFRS() {
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

    public KnnMemoryBasedCFRS(
            CollaborativeSimilarityMeasure similarityMeasure,
            Integer relevanceFactor,
            Number defaultRating,
            boolean inverseFrequency,
            float caseAmplification,
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

    @Override
    public KnnMemoryModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new KnnMemoryModel();
    }

    @Override
    public Recommendations recommendToUser(DatasetLoader<? extends Rating> datasetLoader, KnnMemoryModel model, User user, Set<Item> candidateItems) throws UserNotFound {
        try {
            List<Neighbor> neighbors;
            RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
            neighbors = getNeighbors(datasetLoader, user);
            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), user.getId(), neighbors, candidateItems);
            return new RecommendationsWithNeighbors(user.getTargetId(), ret, neighbors);
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Calcula los vecinos mas cercanos del usuario indicado por parámetro. Para
     * ello, utiliza los valores especificados en los parámetros del algoritmo y
     * los datasets de valoraciones y productos que se indicaron al sistema
     *
     * @param datasetLoader Dataset de valoraciones.
     * @param user usuario para el que se calculan sus vecinos
     * @return Devuelve una lista ordenada por similitud de los vecinos más
     * cercanos al usuario indicado
     * @throws UserNotFound Si el usuario indicado no existe en el conjunto de
     * datos
     */
    public List<Neighbor> getNeighbors(DatasetLoader<? extends Rating> datasetLoader, User user) throws UserNotFound {

        UsersDataset usersDataset = ((UsersDatasetLoader) datasetLoader).getUsersDataset();

        List<KnnMemoryTask> tasks = usersDataset.parallelStream()
                .filter(user2 -> !user.equals(user2))
                .map((userNeighbor) -> new KnnMemoryTask(datasetLoader, user, userNeighbor, this))
                .collect(Collectors.toList());

        MultiThreadExecutionManager<KnnMemoryTask> multiThreadExecutionManager = new MultiThreadExecutionManager<>(
                "Compute neighbors of " + user.getId(),
                tasks,
                KnnMemoryTaskExecutor.class);

        multiThreadExecutionManager.run();

        List<Neighbor> allNeighbors = new ArrayList<>();
        final Collection<KnnMemoryTask> allFinishedTasks = multiThreadExecutionManager.getAllFinishedTasks();
        //Recompongo los resultados.
        for (KnnMemoryTask task : allFinishedTasks) {
            Neighbor neighbor = task.getNeighbor();
            if (neighbor == null) {
                allNeighbors.add(new Neighbor(RecommendationEntity.USER, task.neighborUser, Double.NaN));
            } else {
                allNeighbors.add(neighbor);
            }
        }

        if (Global.isVerboseAnnoying()) {
            Collections.sort(allNeighbors, (Neighbor o1, Neighbor o2) -> o1.getIdNeighbor() - o2.getIdNeighbor());

            Global.showMessageTimestamped("============ All users similarity =================");
            printNeighborhood(user.getId(), allNeighbors);
        }

        allNeighbors.sort(Neighbor.BY_SIMILARITY_DESC);
        return allNeighbors;

    }

    /**
     * Devuelva las recomendaciones, teniendo en cuenta sólo los productos
     * indicados por parámetro, para el usuario activo a partir de los vecinos
     * indicados por parámetro
     *
     * @param ratingsDataset Conjunto de valoraciones.
     * @param idUser Id del usuario activo
     * @param neighbors Vecinos del usuario activo
     * @param candidateItems Lista de productos que se consideran recomendables,
     * es decir, que podrían ser recomendados si la predicción es alta
     * @return Lista de recomendaciones para el usuario, ordenadas por
     * valoracion predicha.
     * @throws UserNotFound Si el usuario activo o alguno de los vecinos
     * indicados no se encuentra en el dataset.
     */
    public Collection<Recommendation> recommendWithNeighbors(
            RatingsDataset<? extends Rating> ratingsDataset,
            Integer idUser,
            List<Neighbor> neighbors,
            Collection<Item> candidateItems) {

        PredictionTechnique predictionTechnique_ = (PredictionTechnique) getParameterValue(KnnMemoryBasedCFRS.PREDICTION_TECHNIQUE);

        int neighborhoodSize_ = ((Number) getParameterValue(KnnMemoryBasedCFRS.NEIGHBORHOOD_SIZE)).intValue();

        List<Neighbor> neighborsWithPositiveSimilarityAndSelected = neighbors.stream()
                .filter((neighbor -> Float.isFinite(neighbor.getSimilarity()) && neighbor.getSimilarity() > 0))
                .collect(Collectors.toList());
        neighborsWithPositiveSimilarityAndSelected.sort(Neighbor.BY_SIMILARITY_DESC);
        neighborsWithPositiveSimilarityAndSelected = neighborsWithPositiveSimilarityAndSelected
                .subList(0, Math.min(neighborsWithPositiveSimilarityAndSelected.size(), neighborhoodSize_));

        if (Global.isVerboseAnnoying()) {
            Global.showMessageTimestamped("============ Selected neighborhood =================");
            printNeighborhood(idUser, neighborsWithPositiveSimilarityAndSelected);
        }

        //Predicción de la valoración
        Collection<Recommendation> recommendations = new LinkedList<>();
        Map<Integer, Map<Integer, ? extends Rating>> ratingsVecinos = neighborsWithPositiveSimilarityAndSelected
                .stream()
                .collect(Collectors.toMap(
                                (neighbor -> neighbor.getIdNeighbor()),
                                (neighbor -> ratingsDataset.getUserRatingsRated(neighbor.getIdNeighbor()))));

        for (Item item : candidateItems) {
            Collection<MatchRating> match = new LinkedList<>();
            for (Neighbor neighbor : neighborsWithPositiveSimilarityAndSelected) {
                Rating rating = ratingsVecinos.get(neighbor.getIdNeighbor()).get(item.getId());
                if (rating != null) {
                    match.add(new MatchRating(RecommendationEntity.USER, neighbor.getIdNeighbor(), item.getId(), rating.ratingValue, neighbor.getSimilarity()));
                }
            }

            try {
                float predicted = predictionTechnique_.predictRating(idUser, item.getId(), match, ratingsDataset);
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
    public KnnMemoryModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return new KnnMemoryModel();
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, KnnMemoryModel model) throws FailureInPersistence {
        //No hay modelo que guardar.
    }

    private void printNeighborhood(int idUser, List<Neighbor> ret) {
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
    public Collection<Recommendation> recommendToUser(
            DatasetLoader<? extends Rating> dataset,
            KnnMemoryModel model,
            Integer idUser,
            Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        User user;
        try {
            user = ((UsersDatasetLoader) dataset).getUsersDataset().get(idUser);
        } catch (EntityNotFound ex) {
            user = new User(idUser);
        }

        Set<Item> candidateItemSet = ((ContentDatasetLoader) dataset).getContentDataset().stream()
                .filter((item -> candidateItems.contains(item.getId())))
                .collect(Collectors.toSet());

        Recommendations recommendToUser = recommendToUser(dataset, model, user, candidateItemSet);
        return recommendToUser.getRecommendations();
    }
}
