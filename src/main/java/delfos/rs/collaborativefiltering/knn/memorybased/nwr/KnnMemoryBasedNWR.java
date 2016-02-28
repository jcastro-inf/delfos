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
package delfos.rs.collaborativefiltering.knn.memorybased.nwr;

import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    public KnnMemoryModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new KnnMemoryModel();
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, KnnMemoryModel model, Integer idUser, Set<Integer> candidateItems) throws UserNotFound {
        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage(new Date().toGMTString() + " --> Recommending for user '" + idUser + "'\n");
        }

        PredictionTechnique predictionTechnique = (PredictionTechnique) getParameterValue(PREDICTION_TECHNIQUE);
        int neighborhoodSize = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        try {
            List<Neighbor> neighbors;
            neighbors = getNeighbors(datasetLoader.getRatingsDataset(), idUser);

            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader, idUser, neighbors, neighborhoodSize, candidateItems, predictionTechnique);
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Finished recommendations for user '" + idUser + "'\n");
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
        ratingsDataset.allUsers().stream().forEach((idNeighbor) -> {
            try {
                tasks.add(new KnnMemoryBasedNWR_Task(ratingsDataset, idUser, idNeighbor, this));
            } catch (UserNotFound ex) {
            }
        });

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

        ret.sort(Neighbor.BY_SIMILARITY_DESC);

        return ret;
    }

    /**
     * Devuelva las recomendaciones, teniendo en cuenta sólo los productos
     * indicados por parámetro, para el usuario activo a partir de los vecinos
     * indicados por parámetro
     *
     * @param datasetLoader Input data.
     * @param idUser Id del usuario activo
     * @param _neighborhood Vecinos del usuario activo
     * @param neighborhoodSize
     * @param candidateIdItems Lista de productos que se consideran
     * recomendables, es decir, que podrían ser recomendados si la predicción es
     * alta
     * @param predictionTechnique
     * @return Lista de recomendaciones para el usuario, ordenadas por
     * valoracion predicha.
     * @throws UserNotFound Si el usuario activo o alguno de los vecinos
     * indicados no se encuentra en el dataset.
     */
    public static Collection<Recommendation> recommendWithNeighbors(
            DatasetLoader<? extends Rating> datasetLoader,
            Integer idUser,
            List<Neighbor> _neighborhood,
            int neighborhoodSize,
            Collection<Integer> candidateIdItems,
            PredictionTechnique predictionTechnique)
            throws UserNotFound {

        List<Neighbor> neighborhood = _neighborhood.stream()
                .filter(neighbor -> !Double.isNaN(neighbor.getSimilarity()))
                .filter(neighbor -> neighbor.getSimilarity() > 0)
                .collect(Collectors.toList());

        neighborhood.sort(Neighbor.BY_SIMILARITY_DESC);

        RatingsDataset ratingsDataset = datasetLoader.getRatingsDataset();
        ContentDataset contentDataset = datasetLoader.getContentDataset();

        Collection<Recommendation> recommendationList = new LinkedList<>();

        List<Item> candidateItems = candidateIdItems.stream()
                .map(idItem -> contentDataset.get(idItem))
                .collect(Collectors.toList());

        for (Item item : candidateItems) {

            Collection<MatchRating> match = new LinkedList<>();
            int numNeighborsUsed = 0;
            try {
                Map<Integer, ? extends Rating> itemRatingsRated = ratingsDataset.getItemRatingsRated(item.getId());
                for (Neighbor neighbor : neighborhood) {

                    Rating rating = itemRatingsRated.get(neighbor.getIdNeighbor());
                    if (rating != null) {
                        match.add(new MatchRating(RecommendationEntity.ITEM, (User) neighbor.getNeighbor(), item, rating.getRatingValue(), neighbor.getSimilarity()));
                        numNeighborsUsed++;
                    }

                    if (numNeighborsUsed >= neighborhoodSize) {
                        break;
                    }
                }

                try {
                    double predicted = predictionTechnique.predictRating(idUser, item.getId(), match, ratingsDataset);
                    recommendationList.add(new Recommendation(item, predicted));

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
