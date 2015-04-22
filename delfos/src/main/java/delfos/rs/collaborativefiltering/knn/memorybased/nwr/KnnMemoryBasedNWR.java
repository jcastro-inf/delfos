package delfos.rs.collaborativefiltering.knn.memorybased.nwr;

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
public class KnnMemoryBasedNWR extends KnnCollaborativeRecommender<KnnMemoryModel> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que añade los parámetros al sistema de recomendación y asigna
     * la medida del coseno y la suma ponderada como medida de similitud y
     * técnica de predicción respectivamente.
     */
    public KnnMemoryBasedNWR() {
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

    public KnnMemoryBasedNWR(
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

    public KnnMemoryBasedNWR(
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
    public KnnMemoryModel build(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new KnnMemoryModel();
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, KnnMemoryModel model, Integer idUser, java.util.Set<Integer> idItemList) throws UserNotFound {
        if (Global.isVerboseAnnoying()) {
            Global.showMessage(new Date().toGMTString() + " --> Recommending for user '" + idUser + "'\n");
        }

        try {
            List<Neighbor> neighbors;
            neighbors = getNeighbors(datasetLoader.getRatingsDataset(), idUser);

            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), idUser, neighbors, idItemList);
            if (Global.isVerboseAnnoying()) {
                Global.showMessage("Finished recommendations for user '" + idUser + "'\n");
            }
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
    public List<Neighbor> getNeighbors(
            RatingsDataset<? extends Rating> ratingsDataset,
            int idUser)
            throws UserNotFound {

        ratingsDataset.getUserRated(idUser);

        List<KnnMemoryBasedNWR_Task> tasks = new ArrayList<>();
        for (int idNeighbor : ratingsDataset.allUsers()) {
            try {
                tasks.add(new KnnMemoryBasedNWR_Task(ratingsDataset, idUser, idNeighbor, this));
            } catch (UserNotFound ex) {
            }
        }

        MultiThreadExecutionManager<KnnMemoryBasedNWR_Task> multiThreadExecutionManager
                = new MultiThreadExecutionManager<>(
                        this.getName() + ":computeNeighborsOf" + idUser,
                        tasks,
                        KnnMemoryBasedNWR_TaskExecutor.class);

        multiThreadExecutionManager.run();

        List<Neighbor> ret = Collections.synchronizedList(new ArrayList<>());
        multiThreadExecutionManager.getAllFinishedTasks().parallelStream().map((task) -> task.getNeighbor()).filter((neighbor) -> (neighbor != null)).forEach((neighbor) -> {
            ret.add(neighbor);
        });
        Collections.sort(ret);

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
    public Collection<Recommendation> recommendWithNeighbors(
            RatingsDataset<? extends Rating> ratingsDataset,
            Integer idUser,
            List<Neighbor> vecinos,
            Collection<Integer> idItemList)
            throws UserNotFound {

        PredictionTechnique predictionTechnique_ = (PredictionTechnique) getParameterValue(KnnMemoryBasedNWR.PREDICTION_TECHNIQUE);

        //Predicción de la valoración
        Collection<Recommendation> recommendationList = new LinkedList<>();

        int numVecinos = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        for (int idItem : idItemList) {
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
    public KnnMemoryModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return new KnnMemoryModel();
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, KnnMemoryModel model) throws FailureInPersistence {
        //No hay modelo que guardar.
    }
}
