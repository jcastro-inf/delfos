package delfos.rs.collaborativefiltering.knn.memorybased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;

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
    public KnnMemoryModel build(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new KnnMemoryModel();
    }

    @Override
    public List<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader,
            KnnMemoryModel model,
            Integer idUser,
            Collection<Integer> idItemList) throws UserNotFound {
        try {
            List<Neighbor> neighbors;
            RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
            neighbors = getNeighbors(ratingsDataset, idUser);
            List<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), idUser, neighbors, idItemList);
            return ret;
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Calcula los vecinos mas cercanos del usuario indicado por parámetro. Para
     * ello, utiliza los valores especificados en los parámetros del algoritmo y
     * los datasets de valoraciones y productos que se indicaron al sistema
     *
     * @param ratingsDataset Dataset de valoraciones.
     * @param idUser id del usuario para el que se calculan sus vecinos
     * @return Devuelve una lista ordenada por similitud de los vecinos más
     * cercanos al usuario indicado
     * @throws UserNotFound Si el usuario indicado no existe en el conjunto de
     * datos
     */
    public List<Neighbor> getNeighbors(RatingsDataset<? extends Rating> ratingsDataset, int idUser) throws UserNotFound {

        ratingsDataset.getUserRated(idUser);

        List<KnnMemoryTask> tasks = new ArrayList<>();
        for (int idNeighbor : ratingsDataset.allUsers()) {
            try {
                tasks.add(new KnnMemoryTask(ratingsDataset, idUser, idNeighbor, this));
            } catch (UserNotFound ex) {
            }
        }

        MultiThreadExecutionManager<KnnMemoryTask> multiThreadExecutionManager = new MultiThreadExecutionManager<>(
                "Compute neighbors of " + idUser,
                tasks,
                KnnMemoryTaskExecutor.class);

        multiThreadExecutionManager.run();

        List<Neighbor> ret = new ArrayList<>();
        //Recompongo los resultados.
        for (KnnMemoryTask task : multiThreadExecutionManager.getAllFinishedTasks()) {
            Neighbor neighbor = task.getNeighbor();
            if (neighbor != null) {
                ret.add(neighbor);
            }
        }
        Collections.sort(ret);

        int neighborhoodSize_ = ((Number) getParameterValue(KnnMemoryBasedCFRS.NEIGHBORHOOD_SIZE)).intValue();
        if (ret.isEmpty()) {
//            Global.showMessage("No se pudieron encontrar vecinos para el usuario "+idUser);
            ret = Collections.emptyList();
        } else {
            ret = ret.subList(0, Math.min(ret.size(), neighborhoodSize_));
        }

        return ret;

    }

    /**
     * Devuelva las recomendaciones, teniendo en cuenta sólo los productos
     * indicados por parámetro, para el usuario activo a partir de los vecinos
     * indicados por parámetro
     *
     * @param ratingsDataset Conjunto de valoraciones.
     * @param idUser Id del usuario activo
     * @param vecinos Vecinos del usuario activo
     * @param idItemList Lista de productos que se consideran recomendables, es
     * decir, que podrían ser recomendados si la predicción es alta
     * @return Lista de recomendaciones para el usuario, ordenadas por
     * valoracion predicha.
     * @throws UserNotFound Si el usuario activo o alguno de los vecinos
     * indicados no se encuentra en el dataset.
     */
    public List<Recommendation> recommendWithNeighbors(RatingsDataset<? extends Rating> ratingsDataset, Integer idUser, List<Neighbor> vecinos, Collection<Integer> idItemList) throws UserNotFound {
        PredictionTechnique predictionTechnique_ = (PredictionTechnique) getParameterValue(KnnMemoryBasedCFRS.PREDICTION_TECHNIQUE);

        //Predicción de la valoración
        List<Recommendation> recommendationList = new LinkedList<>();
        Map<Integer, Map<Integer, ? extends Rating>> ratingsVecinos = new TreeMap<>();
        for (Neighbor ss : vecinos) {
            ratingsVecinos.put(ss.getIdNeighbor(), ratingsDataset.getUserRatingsRated(ss.getIdNeighbor()));
        }

        for (int idItem : idItemList) {
            Collection<MatchRating> match = new LinkedList<>();
            for (Neighbor ss : vecinos) {
                Rating rating = ratingsVecinos.get(ss.getIdNeighbor()).get(idItem);
                if (rating != null) {
                    match.add(new MatchRating(RecommendationEntity.ITEM, ss.getIdNeighbor(), idItem, rating.ratingValue, ss.getSimilarity()));
                }
            }

            try {
                float predicted = predictionTechnique_.predictRating(idUser, idItem, match, ratingsDataset);
                recommendationList.add(new Recommendation(idItem, predicted));

            } catch (CouldNotPredictRating ex) {
            } catch (ItemNotFound ex) {
                Global.showError(ex);
            }
        }

        Collections.sort(recommendationList);

        return recommendationList;
    }

    @Override
    public KnnMemoryModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return new KnnMemoryModel();
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, KnnMemoryModel model) throws FailureInPersistence {
        //No hay modelo que guardar.
    }
}
