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
import delfos.common.parameters.restriction.StringParameter;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
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
import java.util.TreeSet;

/**
 * Implementa un modificador a un sistema de recomendación para que siempre
 * utilice el mismo modelo de recomendación, que se almacena en persistencia en
 * ficheros.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 17-Jun-2013
 * @param <RecommendationModel> Modelo de recomendación
 */
public class RecommenderSystem_fixedFilePersistence<RecommendationModel> extends RecommenderSystemAdapter<RecommendationModel> {

    private static final long serialVersionUID = 1L;
    /**
     * Nombre del archivo de persistencia.
     */
    public static final Parameter persistenceFileName = new Parameter(
            "persistenceFileName",
            new StringParameter("recommendation-model"));
    /**
     * Extensión del archivo de persistencia.
     */
    public static final Parameter persistenceFileType = new Parameter(
            "persistenceFileType",
            new StringParameter("dat"));
    /**
     * Prefijo del archivo de persistencia.
     */
    public static final Parameter persistenceFilePrefix = new Parameter(
            "persistenceFilePrefix",
            new StringParameter(""));
    /**
     * Sufijo del archivo de persistencia.
     */
    public static final Parameter persistenceFileSuffix = new Parameter(
            "persistenceFileSuffix",
            new StringParameter("_fixed"));
    /**
     * Directorio en que se guarda el archivo de persistencia.
     */
    public static final Parameter persistenceFileDirectory = new Parameter(
            "persistenceFileDirectory",
            new DirectoryParameter(
                    new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + "buffered-recommendation-models" + File.separator + "recommendation-model.data").getAbsoluteFile().getParentFile()));
    /**
     * Sistema de recomendación con persistencia en modelo para el que se fija
     * el modelo.
     */
    public static final Parameter recommenderSystem = new Parameter(
            "recommenderSystem",
            new RecommenderSystemParameterRestriction(new KnnModelBasedCFRS(), RecommenderSystem.class));
    /**
     * Objeto para realizar la exclusión mútua en la generación del dataset.
     */
    private final Object exMut = this;

    public RecommenderSystem_fixedFilePersistence() {
        super();
        addParameter(recommenderSystem);
        addParameter(persistenceFileName);
        addParameter(persistenceFileDirectory);
        addParameter(persistenceFilePrefix);
        addParameter(persistenceFileSuffix);
        addParameter(persistenceFileType);
    }

    public <RecommendationModel extends Object> RecommenderSystem_fixedFilePersistence(RecommenderSystem<RecommendationModel> rs) {
        this(rs, new FilePersistence(
                "recommendation-model-" + rs.getAlias().toLowerCase(),
                "data",
                new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + "buffered-recommendation-models" + File.separator + "recommendation-model.data").getAbsoluteFile().getParentFile()));
    }

    public <RecommendationModel extends Object> RecommenderSystem_fixedFilePersistence(RecommenderSystem<RecommendationModel> rs, FilePersistence filePersistence) {
        this();

        if (rs == null) {
            throw new IllegalArgumentException("The recommender system cannot be null");
        }

        if (filePersistence == null) {
            throw new IllegalArgumentException("The file persistence cannot be null");
        }

        setParameterValue(recommenderSystem, rs);
        setParameterValue(persistenceFileName, filePersistence.getFileName());
        setParameterValue(persistenceFileDirectory, filePersistence.getDirectory());
        setParameterValue(persistenceFilePrefix, filePersistence.getPrefix());
        setParameterValue(persistenceFileSuffix, filePersistence.getSuffix() + rs.getAlias().toLowerCase());
        setParameterValue(persistenceFileType, filePersistence.getExtension());

        getRecommenderSystem().addRecommendationModelBuildingProgressListener(this::fireBuildingProgressChangedEvent);
    }

    @Override
    public <RatingType extends Rating> RecommendationModel buildRecommendationModel(DatasetLoader<RatingType> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        synchronized (exMut) {

            RecommendationModel model;

            int ratingsDatasetHashCode = datasetLoader.getRatingsDataset().hashCode();
            String datasetLoaderAlias = datasetLoader.getAlias();

            String suffix = "_datasetLoader=" + datasetLoaderAlias + "_DLHash=" + ratingsDatasetHashCode;

            FilePersistence filePersistenceWithHashSuffix = getFilePersistence().copyWithSuffix(suffix);
            Collection<Long> allItems = new TreeSet<>();
            if (datasetLoader instanceof ContentDatasetLoader) {
                ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
                allItems.addAll(contentDatasetLoader.getContentDataset().allIDs());
            } else {
                allItems.addAll(datasetLoader.getRatingsDataset().allRatedItems());
            }

            try {
                RecommendationModel loadedModel = (RecommendationModel) getRecommenderSystem().loadRecommendationModel(
                        filePersistenceWithHashSuffix,
                        datasetLoader.getRatingsDataset().allUsers(),
                        allItems);
                Global.showMessageTimestamped("Loaded model from file '" + filePersistenceWithHashSuffix.getCompleteFileName() + "'");
                model = loadedModel;
            } catch (FailureInPersistence ex) {
                RecommendationModelBuildingProgressListener listener = this::fireBuildingProgressChangedEvent;

                Global.showWarning("Recommendation model not found: " + filePersistenceWithHashSuffix.getCompleteFileName() + "\n");
                Global.showWarning("REASON: " + ex.getMessage());
                Global.showWarning(ex);
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

            return (RecommendationModel) model;
        }
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public <RatingType extends Rating> Collection<Recommendation> recommendToUser(DatasetLoader<RatingType> datasetLoader, RecommendationModel model, long idUser, java.util.Set<Long> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        Collection<Recommendation> recommendations;

        recommendations = getRecommenderSystem().recommendToUser(datasetLoader, model, idUser, candidateItems);
        return recommendations;
    }

    /**
     * Devuelve el valor del parámetro
     *
     * @return the rs_withFilePersistence
     */
    private RecommenderSystem<Object> getRecommenderSystem() {
        return (RecommenderSystem<Object>) getParameterValue(recommenderSystem);
    }

    /**
     * Devuelve la persistencia en fichero.
     *
     * @return
     */
    private FilePersistence getFilePersistence() {
        final String fileName = (String) getParameterValue(persistenceFileName);
        final String prefix = (String) getParameterValue(persistenceFilePrefix);
        final String sufix = (String) getParameterValue(persistenceFileSuffix);
        final String fileType = (String) getParameterValue(persistenceFileType);
        final File directory = (File) getParameterValue(persistenceFileDirectory);

        return new FilePersistence(fileName, fileType, prefix, sufix, directory);
    }
}
