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
package delfos.rs.bufferedrecommenders;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recommender system that stores the recommendation model generated in a common
 * directory for recommendation models. It also does a cache copy in memory of
 * the recommendation models already loaded.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RecommendationModel> Modelo de recomendación
 */
public class RecommenderSystem_cacheRecommendationModel<RecommendationModel> extends RecommenderSystemAdapter<RecommendationModel> {

    private static final long serialVersionUID = 1L;

    public static final File DEFAULT_DIRECTORY = new File(
            Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "buffered-recommendation-models" + File.separator);

    public static final String extension = "model";
    public static final Parameter RECOMMENDATION_MODELS_DIRECTORY = new Parameter(
            "persistenceFileDirectory",
            new DirectoryParameter(DEFAULT_DIRECTORY));

    public static final Parameter RECOMMENDER_SYSTEM = new Parameter(
            "recommenderSystem",
            new RecommenderSystemParameterRestriction(new KnnModelBasedCFRS(), RecommenderSystem.class));

    private static final Object generalExMut = "GeneralExMut";

    private static final Map<DatasetLoader, Semaphore> datasetLoaderExMuts = new HashMap<>();
    private static final Map<DatasetLoader, Map<RecommenderSystem, Object>> cacheOfRecommendationModels = new HashMap<>();

    public RecommenderSystem_cacheRecommendationModel() {
        super();
        addParameter(RECOMMENDER_SYSTEM);
        addParameter(RECOMMENDATION_MODELS_DIRECTORY);
    }

    public RecommenderSystem_cacheRecommendationModel setRecommenderSystem(RecommenderSystem<RecommendationModel> recommenderSystem) {
        setParameterValue(RECOMMENDER_SYSTEM, recommenderSystem);
        return this;
    }

    public RecommenderSystem_cacheRecommendationModel setDirectory(File directory) {
        setParameterValue(RECOMMENDATION_MODELS_DIRECTORY, directory);
        return this;
    }

    @Override
    public RecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        final RecommenderSystem<Object> recommenderSystem = getRecommenderSystem();
        RecommendationModel model;
        Semaphore exMutThisDatasetLoader;
        try {
            synchronized (generalExMut) {
                if (!datasetLoaderExMuts.containsKey(datasetLoader)) {
                    datasetLoaderExMuts.put(datasetLoader, new Semaphore(1));
                }

                if (!cacheOfRecommendationModels.containsKey(datasetLoader)) {
                    cacheOfRecommendationModels.put(datasetLoader, new HashMap<>());
                }

                if (cacheOfRecommendationModels.get(datasetLoader).containsKey(recommenderSystem)) {
                    RecommendationModel recommendationModel = (RecommendationModel) cacheOfRecommendationModels
                            .get(datasetLoader)
                            .get(recommenderSystem);
                    return recommendationModel;
                }

                exMutThisDatasetLoader = datasetLoaderExMuts.get(datasetLoader);
                exMutThisDatasetLoader.acquire();

            }

            int ratingsDatasetHashCode = datasetLoader.getRatingsDataset().hashCode();
            String datasetLoaderAlias = datasetLoader.getAlias();

            String rsNameIdentifier = "_rsHash=" + recommenderSystem.hashCode();
            String datasetLoaderString = "_datasetLoader=" + datasetLoaderAlias + "_DLHash=" + ratingsDatasetHashCode;

            FilePersistence filePersistenceWithHashSuffix = new FilePersistence(
                    recommenderSystem.getName(),
                    extension,
                    (File) getParameterValue(RECOMMENDATION_MODELS_DIRECTORY))
                    .copyWithSuffix(rsNameIdentifier)
                    .copyWithSuffix(datasetLoaderString);

            try {
                RecommendationModel loadedModel = (RecommendationModel) getRecommenderSystem().loadRecommendationModel(
                        filePersistenceWithHashSuffix,
                        datasetLoader.getUsersDataset().allIDs(),
                        datasetLoader.getContentDataset().allIDs());
                Global.showMessageTimestamped("Loaded model from file '" + filePersistenceWithHashSuffix.getCompleteFileName() + "'");
                model = loadedModel;
            } catch (FailureInPersistence ex) {
                RecommendationModelBuildingProgressListener listener = this::fireBuildingProgressChangedEvent;

                Global.showWarning("Recommendation model not found: " + filePersistenceWithHashSuffix.getCompleteFileName() + "\n");
                Global.showWarning("\tThe recommender system model needs to be constructed.\n");
                getRecommenderSystem().addRecommendationModelBuildingProgressListener(listener);
                try {
                    RecommendationModel computedModel = (RecommendationModel) getRecommenderSystem().buildRecommendationModel(datasetLoader);
                    model = computedModel;
                    getRecommenderSystem().saveRecommendationModel(filePersistenceWithHashSuffix, computedModel);
                } catch (FailureInPersistence ex1) {
                    ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex1);
                    model = null;
                }
                getRecommenderSystem().removeRecommendationModelBuildingProgressListener(listener);
            }

            cacheOfRecommendationModels.get(datasetLoader).put(recommenderSystem, model);
            exMutThisDatasetLoader.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(RecommenderSystem_cacheRecommendationModel.class.getName()).log(Level.SEVERE, null, ex);
            model = buildRecommendationModel(datasetLoader);
        }
        return (RecommendationModel) model;
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        Collection<Recommendation> recommendations;

        recommendations = getRecommenderSystem().recommendToUser(datasetLoader, model, idUser, candidateItems);
        return recommendations;
    }

    /**
     * Devuelve el valor del parámetro
     * {@link RecommenderSystem_fixedFilePersistence#groupRecommenderSystem}.
     *
     * @return the rs_withFilePersistence
     */
    private RecommenderSystem<Object> getRecommenderSystem() {
        return (RecommenderSystem<Object>) getParameterValue(RECOMMENDER_SYSTEM);
    }
}
