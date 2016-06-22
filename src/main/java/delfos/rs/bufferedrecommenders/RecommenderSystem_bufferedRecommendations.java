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
import delfos.common.FileUtilities;
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
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sistema de recomendación que almacena las recomendaciones calculadas en un archivo en disco, para agilizar
 * posteriores ejecuciones. Almacena los archivos en el directorio indicado por parámetro. El nombre de los archivos es
 * el hashCode de las valoraciones del usuario.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 18-Jun-2013
 */
public class RecommenderSystem_bufferedRecommendations extends RecommenderSystemAdapter<Object> {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_RECOMMENDATIONS_EXTENSION = ".buffered.recommendations";
    public static final File DEFAULT_DIRECTORY = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "bufferOfRecommendations" + File.separator
    );

    /**
     * Directorio en que se guarda el archivo de persistencia.
     */
    public static final Parameter BUFFER_DIRECTORY = new Parameter(
            "persistenceFileDirectory",
            new DirectoryParameter(
                    DEFAULT_DIRECTORY
            )
    );
    /**
     * Sistema de recomendación con persistencia en modelo para el que se fija el modelo.
     */
    public static final Parameter RECOMMENDER_SYSTEM = new Parameter(
            "recommenderSystem",
            new RecommenderSystemParameterRestriction(new TryThisAtHomeSVD(5, 1000), RecommenderSystem.class));

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
    public RecommendationsToUser recommendToUser(DatasetLoader<? extends Rating> datasetLoader, Object recommendationModel, User user, Set<Item> candidateItems) {
        Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

        int hashCodeOfRatings = userRatings.hashCode();
        String recommendationsFileName = getPersistenceDirectory().getAbsolutePath() + File.separator + "idUser_" + user.getId() + "_" + hashCodeOfRatings + DEFAULT_RECOMMENDATIONS_EXTENSION;
        File recommendationsFile = new File(recommendationsFileName);

        FileUtilities.createDirectoriesForFile(recommendationsFile);

        RecommendationsToUser recommendations;

        if (!recommendationsFile.exists()) {
            recommendations = getRecommenderSystem().recommendToUser(datasetLoader, recommendationModel, user, candidateItems);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(recommendationsFile))) {
                oos.writeObject(userRatings);
                oos.writeObject(recommendations);
            } catch (IOException ex) {
                Global.showWarning("The serialization of ratings and recommendations had a problem.");
                throw new UnsupportedOperationException(ex);
            }
        } else {

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(recommendationsFile))) {
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

                recommendations = (RecommendationsToUser) ois.readObject();

                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage("The recommendations have been loaded: \n" + recommendations.toString() + "\n");
                }
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (FileNotFoundException ex) {
                //Esta excepción no puede ocurrir nunca, ya que se ha comprobado en el if.
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (IOException ex) {
                Global.showWarning("Cannot read file: " + recommendationsFile.getAbsolutePath());
                ERROR_CODES.CANNOT_READ_RECOMMENDATIONS.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        }

        return recommendations;
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, Object model, Integer idUser, Set<Integer> candidateIdItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        User user = datasetLoader.getUsersDataset().get(idUser);
        Set<Item> candidateItems = candidateIdItems.stream().map(idItem -> datasetLoader.getContentDataset().get(idItem)).collect(Collectors.toSet());
        return recommendToUser(datasetLoader, model, user, candidateItems).getRecommendations();
    }

    public RecommenderSystem getRecommenderSystem() {
        return (RecommenderSystem) getParameterValue(RECOMMENDER_SYSTEM);
    }

    public File getPersistenceDirectory() {
        return (File) getParameterValue(BUFFER_DIRECTORY);
    }
}
