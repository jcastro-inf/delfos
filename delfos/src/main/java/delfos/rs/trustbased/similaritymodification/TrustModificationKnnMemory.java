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
package delfos.rs.trustbased.similaritymodification;

import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.CASE_AMPLIFICATION;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.DEFAULT_RATING;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.DEFAULT_RATING_VALUE;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.INVERSE_FREQUENCY;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.PREDICTION_TECHNIQUE;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.RELEVANCE_FACTOR;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.RELEVANCE_FACTOR_VALUE;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.SIMILARITY_MEASURE;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.trustbased.belieffunctions.BeliefFunction;
import delfos.rs.trustbased.belieffunctions.LinearBelief;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
 * @version 14-abril-2014
 */
public class TrustModificationKnnMemory extends CollaborativeRecommender<Object> {

    private static final long serialVersionUID = 1L;

    public static final Parameter BELIEF_DERIVATION = new Parameter(
            "BELIEF_DERIVATION",
            new ParameterOwnerRestriction(BeliefFunction.class, new LinearBelief()));

    /**
     * Constructor que añade los parámetros al sistema de recomendación y asigna
     * la medida del coseno y la suma ponderada como medida de similitud y
     * técnica de predicción respectivamente.
     */
    public TrustModificationKnnMemory() {
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
        addParameter(BELIEF_DERIVATION);
    }

    public TrustModificationKnnMemory(
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

    @Override
    public Object buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return 1l;
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, Object model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound {

        try {
            List<Neighbor> neighbors;
            neighbors = getNeighbors(datasetLoader.getRatingsDataset(), idUser);

            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), idUser, neighbors, candidateItems);

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

        List<NeighborCalculationTask> tasks = new ArrayList<>();
        for (int idNeighbor : ratingsDataset.allUsers()) {

            tasks.add(new NeighborCalculationTask(ratingsDataset, idUser, idNeighbor, this));

        }

        MultiThreadExecutionManager<NeighborCalculationTask> multiThreadExecutionManager = new MultiThreadExecutionManager<>(
                "Compute neighbors of " + idUser,
                tasks,
                NeighborCalculationCalculator.class);

        multiThreadExecutionManager.run();

        List<Neighbor> ret = new ArrayList<>();
        //Recompongo los resultados.
        for (NeighborCalculationTask task : multiThreadExecutionManager.getAllFinishedTasks()) {
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

        //BeliefTransformation
        BeliefFunction beliefFunction = (BeliefFunction) getParameterValue(BELIEF_DERIVATION);

        List<Neighbor> vecinosTransformados = new ArrayList<>(vecinos.size());
        for (Neighbor neighbor : vecinos) {

            double correlation = neighbor.getSimilarity();
            double trust = beliefFunction.beliefFromCorrelation(correlation);

            vecinosTransformados.add(new Neighbor(RecommendationEntity.USER, neighbor.getIdNeighbor(), trust));
        }

        for (int idItem : candidateItems) {
            Collection<MatchRating> match = new LinkedList<>();

            int numNeighborsUsed = 0;
            for (Neighbor ss : vecinosTransformados) {

                Rating rating = ratingsDataset.getUserRatingsRated(ss.getIdNeighbor()).get(idItem);
                if (rating != null) {
                    match.add(new MatchRating(RecommendationEntity.ITEM, ss.getIdNeighbor(), idItem, rating.getRatingValue(), ss.getSimilarity()));
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
            } catch (ItemNotFound ex) {
                Global.showError(ex);
            }
        }

        return recommendationList;
    }

    @Override
    public Object loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return 1l;
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, Object model) throws FailureInPersistence {
        //No hay modelo que guardar.
    }
}
