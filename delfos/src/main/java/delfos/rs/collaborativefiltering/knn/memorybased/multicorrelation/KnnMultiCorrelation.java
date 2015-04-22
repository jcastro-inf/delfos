package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
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
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.RelevanceFactor;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;

/**
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class KnnMultiCorrelation extends CollaborativeRecommender<KnnMultiCorrelation_Model> {

    static {

        ParameterOwnerRestriction similarityMeasure = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient()));

        ParameterOwnerRestriction preFilterSimilarityMeasure = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                new RelevanceFactor(30));

        PREFILTER_SIMILARITY_MEASURE = new Parameter(
                "PreFilter_Similarity_measure",
                preFilterSimilarityMeasure);

        MULTI_CORRELATION_SIMILARITY_MEASURE = new Parameter(
                "MULTI_CORRELATION_SIMILARITY_MEASURE",
                similarityMeasure);

    }

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para indicar la medida de similitud que el sistema de
     * recomendación utiliza para filtrar los vecinos más cercanos. Si no se
     * modifica, su valor por defecto es RelevanceFactor(30)
     */
    public static final Parameter PREFILTER_SIMILARITY_MEASURE;

    /**
     * Parámetro para indicar la medida de similitud que el sistema de
     * recomendación utiliza para el cálculo de los vecinos más cercanos. Si no
     * se modifica, su valor por defecto es el coeficiente de correlación de
     * pearson.
     */
    public static final Parameter MULTI_CORRELATION_SIMILARITY_MEASURE;

    public KnnMultiCorrelation() {
        super();
        addParameter(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE);
        addParameter(PREFILTER_SIMILARITY_MEASURE);
        addParameter(MULTI_CORRELATION_SIMILARITY_MEASURE);
        addParameter(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE);
    }

    public KnnMultiCorrelation(
            UserUserSimilarity similarityMeasure,
            int neighborhoodSize,
            PredictionTechnique predictionTechnique) {

        this();

        setParameterValue(MULTI_CORRELATION_SIMILARITY_MEASURE, similarityMeasure);
        setParameterValue(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE, neighborhoodSize);
        setParameterValue(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE, predictionTechnique);
    }

    @Override
    public KnnMultiCorrelation_Model build(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new KnnMultiCorrelation_Model();
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, KnnMultiCorrelation_Model model, Integer idUser, java.util.Set<Integer> idItemList) throws UserNotFound {

        if (Global.isVerboseAnnoying()) {
            Global.showMessage(new Date().toGMTString() + " --> Recommending for user '" + idUser + "'\n");
        }

        try {
            List<Neighbor> neighbors = getNeighbors(datasetLoader, idUser);

            List<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), idUser, neighbors, idItemList);
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
     * @param datasetLoader Dataset de entrada.
     * @param idUser id del usuario para el que se calculan sus vecinos
     * @return Devuelve una lista ordenada por similitud de los vecinos más
     * cercanos al usuario indicado
     * @throws UserNotFound Si el usuario indicado no existe en el conjunto de
     * datos
     */
    public List<Neighbor> getNeighbors(DatasetLoader<? extends Rating> datasetLoader, int idUser) throws UserNotFound {

        List<KnnMultiCorrelation_Task> tasks = new ArrayList<>();
        for (int idNeighbor : datasetLoader.getRatingsDataset().allUsers()) {
            try {
                tasks.add(new KnnMultiCorrelation_Task(datasetLoader, idUser, idNeighbor, this));
            } catch (UserNotFound ex) {
            }
        }

        MultiThreadExecutionManager<KnnMultiCorrelation_Task> multiThreadExecutionManager = new MultiThreadExecutionManager<>(
                this.getAlias() + ":computeNeighborsOf" + idUser,
                tasks,
                KnnMultiCorrelation_SingleNeighborCalculator.class);

        multiThreadExecutionManager.run();

        List<Neighbor> ret = new ArrayList<>();
        //Recompongo los resultados.
        for (KnnMultiCorrelation_Task task : multiThreadExecutionManager.getAllFinishedTasks()) {
            Neighbor neighbor = task.getNeighbor();
            if (neighbor != null) {
                ret.add(neighbor);
            }
        }
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
    public List<Recommendation> recommendWithNeighbors(
            RatingsDataset<? extends Rating> ratingsDataset,
            Integer idUser,
            List<Neighbor> vecinos,
            Collection<Integer> idItemList)
            throws UserNotFound {

        PredictionTechnique predictionTechnique_ = (PredictionTechnique) getParameterValue(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE);

        //Predicción de la valoración
        List<Recommendation> recommendationList = new LinkedList<>();

        int numVecinos = (Integer) getParameterValue(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE);

        for (int idItem : idItemList) {
            Collection<MatchRating> match = new LinkedList<>();

            int numNeighborsUsed = 0;

            try {
                Map<Integer, ? extends Rating> itemRatingsRated = ratingsDataset.getItemRatingsRated(idItem);
                for (Neighbor neighbor : vecinos) {

                    Rating rating = itemRatingsRated.get(neighbor.getIdNeighbor());
                    if (rating != null) {
                        match.add(new MatchRating(RecommendationEntity.ITEM, neighbor.getIdNeighbor(), idItem, rating.ratingValue, neighbor.getSimilarity()));
                        numNeighborsUsed++;
                    }

                    if (numNeighborsUsed >= numVecinos) {
                        break;
                    }
                }

                try {
                    double predicted = predictionTechnique_.predictRating(idUser, idItem, match, ratingsDataset);
                    recommendationList.add(new Recommendation(idItem, predicted));

                } catch (CouldNotPredictRating ex) {
                }
            } catch (ItemNotFound ex) {
                Global.showError(ex);
            }
        }

        Collections.sort(recommendationList);

        return recommendationList;
    }

    @Override
    public KnnMultiCorrelation_Model loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return new KnnMultiCorrelation_Model();
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, KnnMultiCorrelation_Model model) throws FailureInPersistence {
        //No hay modelo que guardar.

    }
}
