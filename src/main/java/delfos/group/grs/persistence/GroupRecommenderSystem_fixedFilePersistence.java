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
package delfos.group.grs.persistence;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.common.parameters.restriction.StringParameter;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;
import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementa un modificador a un sistema de recomendación para que siempre
 * utilice el mismo modelo de recomendación, que se almacena en persistencia en
 * ficheros.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 16-May-2013
 */
public class GroupRecommenderSystem_fixedFilePersistence extends GroupRecommenderSystemAdapter<Object, Object> {

    private static final long serialVersionUID = 1L;
    /**
     * Nombre del archivo de persistencia.
     */
    public static final Parameter persistenceFileName = new Parameter(
            "persistenceFileName",
            new StringParameter("rsFixedModel"));
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
            new DirectoryParameter(new File("file").getAbsoluteFile().getParentFile()));
    /**
     * Sistema de recomendación con persistencia en modelo para el que se fija
     * el modelo.
     */
    public static final Parameter groupRecommenderSystem = new Parameter(
            "groupRecommenderSystem",
            new RecommenderSystemParameterRestriction(new AggregationOfIndividualRatings(new KnnModelBasedCFRS()), GroupRecommenderSystem.class));
    /**
     * Objeto para realizar la exclusión mútua en la generación del dataset.
     */
    private final Object exMut = this;
    private Object RecommendationModel = null;

    public GroupRecommenderSystem_fixedFilePersistence() {
        super();
        addParameter(groupRecommenderSystem);
        addParameter(persistenceFileName);
        addParameter(persistenceFileDirectory);
        addParameter(persistenceFilePrefix);
        addParameter(persistenceFileSuffix);
        addParameter(persistenceFileType);
    }

    public GroupRecommenderSystem_fixedFilePersistence(GroupRecommenderSystem grs, FilePersistence filePersistence) {
        this();

        if (grs == null) {
            throw new IllegalArgumentException("The recommender system cannot be null");
        }

        if (filePersistence == null) {
            throw new IllegalArgumentException("The file persistence cannot be null");
        }

        setParameterValue(groupRecommenderSystem, grs);
        setParameterValue(persistenceFileName, filePersistence.getFileName());
        setParameterValue(persistenceFileDirectory, filePersistence.getDirectory());
        setParameterValue(persistenceFilePrefix, filePersistence.getPrefix());
        setParameterValue(persistenceFileSuffix, filePersistence.getSuffix());
        setParameterValue(persistenceFileType, filePersistence.getExtension());

        getGroupRecommenderSystem().addRecommendationModelBuildingProgressListener(new RecommendationModelBuildingProgressListener() {
            @Override
            public void buildingProgressChanged(String actualJob, int percent, long remainingTime) {
                fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
            }
        });
    }

    @Override
    public Object buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        synchronized (exMut) {
            if (RecommendationModel == null) {

                Set<Long> allItems = datasetLoader.getContentDataset().stream()
                        .map(item -> item.getId())
                        .collect(Collectors.toSet());

                try {
                    Object loadedModel = getGroupRecommenderSystem().loadRecommendationModel(
                            getFilePersistence(),
                            datasetLoader.getRatingsDataset().allUsers(),
                            allItems);
                    this.RecommendationModel = loadedModel;
                    return loadedModel;
                } catch (Exception ex) {

                    Global.showWarning(ex);
                    RecommendationModelBuildingProgressListener listener = this::fireBuildingProgressChangedEvent;
                    Global.showWarning("Recommendation model not found: \n\tThe recommender system model needs to be constructed.\n");
                    getGroupRecommenderSystem().addRecommendationModelBuildingProgressListener(listener);
                    try {
                        RecommendationModel = getGroupRecommenderSystem().buildRecommendationModel(datasetLoader);
                        getGroupRecommenderSystem().saveRecommendationModel(getFilePersistence(), RecommendationModel);
                    } catch (FailureInPersistence ex1) {
                        ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex1);
                    }
                    getGroupRecommenderSystem().removeRecommendationModelBuildingProgressListener(listener);
                }
            }
            return RecommendationModel;
        }
    }

    @Override
    public <RatingType extends Rating> Object buildGroupModel(DatasetLoader<RatingType> datasetLoader, Object RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        return getGroupRecommenderSystem().buildGroupModel(datasetLoader, RecommendationModel, groupOfUsers);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getGroupRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, Object RecommendationModel, Object groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        GroupRecommendations recommendations = getGroupRecommenderSystem().recommendOnly(datasetLoader, RecommendationModel, groupModel, groupOfUsers, candidateItems);
        return recommendations;
    }

    /**
     * Devuelve el valor del parámetro
     *
     * @return the rs_withFilePersistence
     */
    private GroupRecommenderSystem getGroupRecommenderSystem() {
        return (GroupRecommenderSystem) getParameterValue(groupRecommenderSystem);
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
