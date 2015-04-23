package delfos.group.grs.persistence;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.rs.RecommenderSystemBuildingProgressListener;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;

/**
 * Implementa un modificador a un sistema de recomendación para que siempre
 * utilice el mismo modelo de recomendación, que se almacena en persistencia en
 * ficheros.
 *
* @author Jorge Castro Gallardo
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

        getGroupRecommenderSystem().addBuildingProgressListener(new RecommenderSystemBuildingProgressListener() {
            @Override
            public void buildingProgressChanged(String actualJob, int percent, long remainingTime) {
                fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
            }
        });
    }

    @Override
    public Object build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        synchronized (exMut) {
            if (RecommendationModel == null) {

                Set<Integer> allItems = new TreeSet<Integer>();
                if (datasetLoader instanceof ContentDatasetLoader) {
                    ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
                    allItems.addAll(contentDatasetLoader.getContentDataset().allID());
                } else {
                    allItems.addAll(datasetLoader.getRatingsDataset().allRatedItems());
                }

                try {
                    Object loadedModel = getGroupRecommenderSystem().loadModel(
                            getFilePersistence(),
                            datasetLoader.getRatingsDataset().allUsers(),
                            allItems);
                    this.RecommendationModel = loadedModel;
                    return loadedModel;
                } catch (Exception ex) {

                    Global.showWarning(ex);
                    RecommenderSystemBuildingProgressListener listener = new RecommenderSystemBuildingProgressListener() {
                        @Override
                        public void buildingProgressChanged(String actualJob, int percent, long remainingTime) {
                            fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
                        }
                    };
                    Global.showWarning("Recommendation model not found: \n\tThe recommender system model needs to be constructed.\n");
                    getGroupRecommenderSystem().addBuildingProgressListener(listener);
                    try {
                        RecommendationModel = getGroupRecommenderSystem().build(datasetLoader);
                        getGroupRecommenderSystem().saveModel(getFilePersistence(), RecommendationModel);
                    } catch (FailureInPersistence ex1) {
                        ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex1);
                    }
                    getGroupRecommenderSystem().removeBuildingProgressListener(listener);
                }
            }
            return RecommendationModel;
        }
    }

    @Override
    public Object buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, Object RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        return getGroupRecommenderSystem().buildGroupModel(datasetLoader, RecommendationModel, groupOfUsers);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getGroupRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, Object RecommendationModel, Object groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        Collection<Recommendation> recommendations;
        recommendations = getGroupRecommenderSystem().recommendOnly(datasetLoader, RecommendationModel, groupModel, groupOfUsers, candidateItems);
        return recommendations;
    }

    /**
     * Devuelve el valor del parámetro
     * {@link RecommenderSystem_fixedFilePersistence#groupRecommenderSystem}.
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
