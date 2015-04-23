package delfos.rs.bufferedrecommenders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.recommendation.Recommendation;

/**
 * Sistema de recomendación que almacena las recomendaciones calculadas en un
 * archivo en disco, para agilizar posteriores ejecuciones. Almacena los
 * archivos en el directorio indicado por parámetro. El nombre de los archivos
 * es el hashCode de las valoraciones del usuario.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 18-Jun-2013
 */
public class RecommenderSystem_bufferedRecommendations extends RecommenderSystemAdapter<Object> {

    private static final long serialVersionUID = 1L;
    /**
     * Directorio en que se guarda el archivo de persistencia.
     */
    public static final Parameter BUFFER_DIRECTORY = new Parameter(
            "persistenceFileDirectory",
            new DirectoryParameter(new File("bufferOfRecommendations" + File.separator).getAbsoluteFile()));
    /**
     * Sistema de recomendación con persistencia en modelo para el que se fija
     * el modelo.
     */
    public static final Parameter RECOMMENDER_SYSTEM = new Parameter(
            "recommenderSystem",
            new RecommenderSystemParameterRestriction(new TryThisAtHomeSVD(5, 1000), RecommenderSystem.class));
    private static final String extension = ".buffered.recommendations";

    public RecommenderSystem_bufferedRecommendations() {
        super();
        addParameter(BUFFER_DIRECTORY);
        addParameter(RECOMMENDER_SYSTEM);
    }

    public RecommenderSystem_bufferedRecommendations(File directory, RecommenderSystem recommenderSystem) {
        this();
        setParameterValue(BUFFER_DIRECTORY, directory);
        setParameterValue(RECOMMENDER_SYSTEM, recommenderSystem);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public Object buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        RecommendationModelBuildingProgressListener listener
                = (String actualJob, int percent, long remainingTime) -> {
                    fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
                };

        getRecommenderSystem().addRecommendationModelBuildingProgressListener(listener);
        Object model = getRecommenderSystem().buildRecommendationModel(datasetLoader);
        getRecommenderSystem().removeRecommendationModelBuildingProgressListener(listener);

        if (!getPersistenceDirectory().exists()) {
            getPersistenceDirectory().mkdir();
        }

        return model;
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, Object model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);

        int hashCodeOfRatings = userRatings.hashCode();
        String fileName = getPersistenceDirectory().getAbsolutePath() + File.separator + "idUser_" + idUser + "_" + hashCodeOfRatings + extension;
        File file = new File(fileName);

        file.getParentFile().mkdirs();

        Collection<Recommendation> recommendations;

        if (!file.exists()) {
            recommendations = getRecommenderSystem().recommendToUser(datasetLoader, model, idUser, candidateItems);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(userRatings);
                oos.writeObject(recommendations);
            } catch (IOException ex) {
                Global.showWarning("The serialization of ratings and recommendations had a problem.");
                throw new UnsupportedOperationException(ex);
            }
        } else {

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Map<Integer, ? extends Rating> userRatings_file = (Map<Integer, ? extends Rating>) ois.readObject();
                if (userRatings_file.hashCode() != hashCodeOfRatings) {
                    Global.showWarning("The hash code in the name of loaded file (" + hashCodeOfRatings
                            + ") does not match the hash code of the ratings (" + userRatings_file.hashCode() + ")\n");

                    Exception ex = new IllegalStateException("The hash code in the name of loaded file (" + hashCodeOfRatings
                            + ") does not match the hash code of the ratings (" + userRatings_file.hashCode() + ")\n");
                    Global.showError(ex);
                }

                if (!userRatings.equals(userRatings_file)) {
                    Global.showWarning("The ratings have the same hash code " + hashCodeOfRatings + "but are different:\n"
                            + userRatings.toString() + "\n"
                            + userRatings_file.toString() + "\n");
                }

                recommendations = (Collection<Recommendation>) ois.readObject();

                if (Global.isVerboseAnnoying()) {
                    Global.showMessage("The recommendations have been loaded: \n" + recommendations.toString() + "\n");
                }
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (FileNotFoundException ex) {
                //Esta excepción no puede ocurrir nunca, ya que se ha comprobado en el if.
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (IOException ex) {
                Global.showWarning("Cannot read file: " + file.getAbsolutePath());
                ERROR_CODES.CANNOT_READ_RECOMMENDATIONS.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        }

        return recommendations;
    }

    public RecommenderSystem getRecommenderSystem() {
        return (RecommenderSystem) getParameterValue(RECOMMENDER_SYSTEM);
    }

    public File getPersistenceDirectory() {
        return (File) getParameterValue(BUFFER_DIRECTORY);
    }
}
