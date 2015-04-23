package delfos.rs.bufferedrecommenders;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
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
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemAdapter;
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
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
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
            new DirectoryParameter(new File("buffered-recommendation-models" + File.separator + "recommendation-model.data").getAbsoluteFile().getParentFile()));
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
                new File("buffered-recommendation-models" + File.separator + "recommendation-model.data").getAbsoluteFile().getParentFile()));
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

        getRecommenderSystem().addBuildingProgressListener(this::fireBuildingProgressChangedEvent);
    }

    @Override
    public RecommendationModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        synchronized (exMut) {

            RecommendationModel model;

            int ratingsDatasetHashCode = datasetLoader.getRatingsDataset().hashCode();
            String datasetLoaderAlias = datasetLoader.getAlias();

            String suffix = "_datasetLoader=" + datasetLoaderAlias + "_DLHash=" + ratingsDatasetHashCode;

            FilePersistence filePersistenceWithHashSuffix = getFilePersistence().copyWithSuffix(suffix);
            Collection<Integer> allItems = new TreeSet<>();
            if (datasetLoader instanceof ContentDatasetLoader) {
                ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
                allItems.addAll(contentDatasetLoader.getContentDataset().allID());
            } else {
                allItems.addAll(datasetLoader.getRatingsDataset().allRatedItems());
            }

            try {
                RecommendationModel loadedModel = (RecommendationModel) getRecommenderSystem().loadModel(
                        filePersistenceWithHashSuffix,
                        datasetLoader.getRatingsDataset().allUsers(),
                        allItems);
                Global.showMessageTimestamped("Loaded model from file '" + filePersistenceWithHashSuffix.getCompleteFileName() + "'");
                model = loadedModel;
            } catch (FailureInPersistence ex) {
                RecommenderSystemBuildingProgressListener listener = this::fireBuildingProgressChangedEvent;

                Global.showWarning("Recommendation model not found: " + filePersistenceWithHashSuffix.getCompleteFileName() + "\n");
                Global.showWarning("REASON");
                Global.showWarning(ex);
                Global.showWarning("\tThe recommender system model needs to be constructed.\n");
                getRecommenderSystem().addBuildingProgressListener(listener);
                try {
                    RecommendationModel computedModel = (RecommendationModel) getRecommenderSystem().build(datasetLoader);
                    model = computedModel;
                    getRecommenderSystem().saveModel(filePersistenceWithHashSuffix, computedModel);
                } catch (FailureInPersistence ex1) {
                    ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex1);
                    model = null;
                }
                getRecommenderSystem().removeBuildingProgressListener(listener);
            }

            return (RecommendationModel) model;
        }
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model, Integer idUser, java.util.Set<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        Collection<Recommendation> recommendations;

        recommendations = getRecommenderSystem().recommendOnly(datasetLoader, model, idUser, idItemList);
        return recommendations;
    }

    /**
     * Devuelve el valor del parámetro
     * {@link RecommenderSystem_fixedFilePersistence#groupRecommenderSystem}.
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
