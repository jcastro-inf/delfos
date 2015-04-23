package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation.jaccard;

import delfos.common.Global;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.RelevanceFactor;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class RSTest extends CollaborativeRecommender<RSTest_Model> {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para almacenar el número de vecinos que se tienen en cuenta
     * para la predicción de la valoración. Si no se modifica, su valor por
     * defecto es 20
     */
    public static final Parameter NEIGHBORHOOD_SIZE = new Parameter("Neighborhood_size", new IntegerParameter(1, 999999, 20));

    public static final Parameter NEIGHBORHOOD_FACTOR = new Parameter("Neighborhood_factor", new IntegerParameter(1, 999999, 2));

    private static Parameter PREFILTER_SIMILARITY_MEASURE;
    /**
     * Parámetro para indicar la medida de similitud que el sistema de
     * recomendación utiliza para el cálculo de los vecinos más cercanos. Si no
     * se modifica, su valor por defecto es la suma ponderada
     * ({@link CosineCoefficient})
     */
    public static final Parameter SIMILARITY_MEASURE;
    /**
     * Parámetro para indicar la técnica de predicción que el sistema de
     * recomendación utiliza para la predicción de las valoraciones. Si no se
     * modifica, su valor por defecto es la suma ponderada ({@link WeightedSum})
     */
    public static final Parameter PREDICTION_TECHNIQUE = new Parameter(
            "Prediction_technique",
            new ParameterOwnerRestriction(
                    PredictionTechnique.class,
                    new WeightedSum()
            ));

    static {

        ParameterOwnerRestriction parameterOwnerRestriction = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient()));

        SIMILARITY_MEASURE = new Parameter(
                "Similarity_measure",
                parameterOwnerRestriction);
        ParameterOwnerRestriction preFilterSimilarityMeasure = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                new RelevanceFactor(30));

        PREFILTER_SIMILARITY_MEASURE = new Parameter(
                "PreFilter_Similarity_measure",
                preFilterSimilarityMeasure);

    }

    public RSTest() {
        super();
        addParameter(NEIGHBORHOOD_SIZE);
        addParameter(NEIGHBORHOOD_FACTOR);
        addParameter(PREFILTER_SIMILARITY_MEASURE);
        addParameter(SIMILARITY_MEASURE);
        addParameter(PREDICTION_TECHNIQUE);
    }

    public RSTest(
            UserUserSimilarity preFilterSimilarityMeasure,
            UserUserSimilarity similarityMeasure,
            int neighborhoodSize,
            PredictionTechnique predictionTechnique) {

        this();
        setParameterValue(PREFILTER_SIMILARITY_MEASURE, preFilterSimilarityMeasure);
        setParameterValue(SIMILARITY_MEASURE, similarityMeasure);
        setParameterValue(NEIGHBORHOOD_SIZE, neighborhoodSize);
        setParameterValue(PREDICTION_TECHNIQUE, predictionTechnique);
    }

    @Override
    public RSTest_Model build(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new RSTest_Model();
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RSTest_Model model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound {

        if (Global.isVerboseAnnoying()) {
            Global.showMessageTimestamped(this.getAlias() + " --> Recommending for user '" + idUser + "'\n");
        }

        try {
            List<Neighbor> neighbors = getNeighbors(datasetLoader, idUser);

            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), idUser, neighbors, candidateItems);
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

        LinkedList<Neighbor> preFilterNeighbors = new LinkedList<>();

        final UserUserSimilarity preFilterSimilarityMeasure = (UserUserSimilarity) getParameterValue(PREFILTER_SIMILARITY_MEASURE);
        final UserUserSimilarity similarityMeasure = (UserUserSimilarity) getParameterValue(SIMILARITY_MEASURE);
        final int overNeighborhoodMultiplier = (Integer) getParameterValue(NEIGHBORHOOD_FACTOR);
        final int neighborhoodSize = (Integer) getParameterValue(NEIGHBORHOOD_SIZE) * overNeighborhoodMultiplier;

        for (int idNeighbor : datasetLoader.getRatingsDataset().allUsers()) {
            if (idNeighbor == idUser) {
                continue;
            }

            double preFilterSimilarity;
            try {
                preFilterSimilarity = preFilterSimilarityMeasure.similarity(datasetLoader, idUser, idNeighbor);
                if (preFilterSimilarity != 0) {
                    preFilterNeighbors.add(new Neighbor(RecommendationEntity.USER, idNeighbor, preFilterSimilarity));
                }
            } catch (CouldNotComputeSimilarity ex) {

            }
        }

        Collections.sort(preFilterNeighbors);
        LinkedList<Neighbor> neighbors = new LinkedList<>();

        if (preFilterNeighbors.isEmpty()) {
            System.out.println("preFilterNeighbors is empty");
        }

        //Recorro los vecinos por jaccard,
        Neighbor preFilterNeighbor = preFilterNeighbors.removeFirst();

        float minPreFilter = 0;

        while (preFilterNeighbor != null && preFilterNeighbor.getSimilarity() > minPreFilter) {
            //Añado vecino con sim jaccard* similarity
            int idNeighbor = preFilterNeighbor.getIdNeighbor();
            float preFilterSimilarity = preFilterNeighbor.getSimilarity();
            try {
                double similarity = similarityMeasure.similarity(datasetLoader, idUser, idNeighbor);

                if (preFilterSimilarity * similarity > 0) {
                    neighbors.add(new Neighbor(RecommendationEntity.USER, idNeighbor, preFilterSimilarity * similarity));
                }

            } catch (CouldNotComputeSimilarity ex) {

            }

            if (neighbors.size() > neighborhoodSize) {
                Collections.sort(neighbors);

                while (neighbors.size() > neighborhoodSize) {
                    neighbors.removeLast();
                }

                float minSimilarity = neighbors.getLast().getSimilarity();
                minPreFilter = minSimilarity;
            }

            if (preFilterNeighbors.isEmpty()) {
                preFilterNeighbor = null;
            } else {
                preFilterNeighbor = preFilterNeighbors.removeFirst();
            }
        }

        Collections.sort(neighbors);

        return neighbors;
    }

    /**
     * Devuelva las recomendaciones, teniendo en cuenta sólo los productos
     * indicados por parámetro, para el usuario activo a partir de los vecinos
     * indicados por parámetro
     *
     * @param ratingsDataset Conjunto de valoraciones.
     * @param idUser Id del usuario activo
     * @param vecinos Vecinos del usuario activo
     * @param candidateItems Lista de productos que se consideran recomendables, es
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
            Collection<Integer> candidateItems)
            throws UserNotFound {

        PredictionTechnique predictionTechnique_ = (PredictionTechnique) getParameterValue(PREDICTION_TECHNIQUE);

        //Predicción de la valoración
        Collection<Recommendation> recommendationList = new LinkedList<>();

        int numVecinos = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        for (int idItem : candidateItems) {
            Collection<MatchRating> match = new LinkedList<>();

            int numNeighborsUsed = 0;

            try {
                Map<Integer, ? extends Rating> itemRatingsRated = ratingsDataset.getItemRatingsRated(idItem);
                for (Neighbor neighbor : vecinos) {

                    final int idNeighbor = neighbor.getIdNeighbor();
                    final float similarity = neighbor.getSimilarity();

                    Rating rating = itemRatingsRated.get(neighbor.getIdNeighbor());

                    if (rating != null) {
                        float ratingValue = rating.ratingValue.floatValue();
                        match.add(new MatchRating(
                                RecommendationEntity.ITEM,
                                idNeighbor,
                                idItem,
                                ratingValue,
                                similarity));
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

        return recommendationList;
    }

    @Override
    public RSTest_Model loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return new RSTest_Model();
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, RSTest_Model model) throws FailureInPersistence {
        //No hay modelo que guardar.

    }
}
