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
package delfos.similaritymeasures.useruser;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.similaritymeasures.SimilarityMeasureAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 *
 * @version 08-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class UserUserSimilarity_buffered extends SimilarityMeasureAdapter implements UserUserSimilarity {

    static {
        ParameterOwnerRestriction parameterOwnerRestriction = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                new UserUserSimilarityWrapper());
        WRAPPED_SIMILARITY = new Parameter(
                "WrappedSimilarity",
                parameterOwnerRestriction);
    }

    public static final Parameter WRAPPED_SIMILARITY;
    /**
     * Directorio en que se guarda el archivo de persistencia.
     */
    public static final Parameter BUFFER_DIRECTORY = new Parameter(
            "persistenceFileDirectory",
            new DirectoryParameter(new File("bufferOfRecommendations" + File.separator).getAbsoluteFile()));
    private static final String extension = ".buffered.similarity";

    private UserUserSimilarity userUserSimilarity;

    public UserUserSimilarity_buffered() {
        super();
        addParameter(WRAPPED_SIMILARITY);
        addParameter(BUFFER_DIRECTORY);

        addParammeterListener(() -> {
            UserUserSimilarity_buffered.this.userUserSimilarity = (UserUserSimilarity) getParameterValue(WRAPPED_SIMILARITY);
        });
    }

    public UserUserSimilarity_buffered(File bufferDirectory, UserUserSimilarity userUserSimilarity) {
        this();
        setParameterValue(WRAPPED_SIMILARITY, userUserSimilarity);
        setParameterValue(BUFFER_DIRECTORY, bufferDirectory);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, long idUser1, long idUser2) {

        Map<Long, ? extends Rating> user1Ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
        Map<Long, ? extends Rating> user2Ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

        final int hashCode_user1Ratings = user1Ratings.hashCode();
        final int hashCode_user2Ratings = user2Ratings.hashCode();

        String fileName
                = getPersistenceDirectory().getAbsolutePath() + File.separator
                + "idUser1_" + idUser1 + "_" + hashCode_user1Ratings
                + "idUser2_" + idUser2 + "_" + hashCode_user2Ratings
                + extension;
        File file = new File(fileName);

        file.getParentFile().mkdirs();

        double similarity;

        if (!file.exists()) {

            similarity = userUserSimilarity.similarity(datasetLoader, idUser1, idUser2);

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(user1Ratings);
                oos.writeObject(user2Ratings);
                oos.writeObject(similarity);
            } catch (IOException ex) {
                Global.showWarning("The serialization of ratings and similarity had a problem.");
                throw new UnsupportedOperationException(ex);
            }
        } else {

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Map<Long, ? extends Rating> user1Ratings_file = (Map<Long, ? extends Rating>) ois.readObject();
                if (user1Ratings_file.hashCode() != hashCode_user1Ratings) {
                    Global.showWarning("The hash code in the name of loaded file (" + hashCode_user1Ratings
                            + ") does not match the hash code of user1 ratings (" + user1Ratings_file.hashCode() + ")\n");

                    Exception ex = new IllegalStateException("The hash code in the name of loaded file (" + hashCode_user1Ratings
                            + ") does not match the hash code of user1 ratings (" + user1Ratings_file.hashCode() + ")\n");
                    Global.showError(ex);
                }

                Map<Long, ? extends Rating> user2Ratings_file = (Map<Long, ? extends Rating>) ois.readObject();
                if (user2Ratings_file.hashCode() != hashCode_user2Ratings) {
                    Global.showWarning("The hash code in the name of loaded file (" + hashCode_user2Ratings
                            + ") does not match the hash code of user2 ratings (" + user2Ratings_file.hashCode() + ")\n");

                    Exception ex = new IllegalStateException("The hash code in the name of loaded file (" + hashCode_user2Ratings
                            + ") does not match the hash code of user2 ratings (" + user2Ratings_file.hashCode() + ")\n");
                    Global.showError(ex);
                }

                if (!user1Ratings.equals(user1Ratings_file)) {
                    Global.showWarning("The ratings have the same hash code " + hashCode_user1Ratings + "but are different:\n"
                            + user1Ratings.toString() + "\n"
                            + user1Ratings_file.toString() + "\n");
                }

                if (!user2Ratings.equals(user2Ratings_file)) {
                    Global.showWarning("The ratings have the same hash code " + hashCode_user2Ratings + "but are different:\n"
                            + user2Ratings.toString() + "\n"
                            + user2Ratings_file.toString() + "\n");
                }

                similarity = (Double) ois.readObject();

//                if (Global.isVerboseAnnoying()) {
//                    Global.showInfoMessage("The similarity has been loaded: \n" + similarity + "\n");
//                }
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

        return similarity;

    }

    public File getPersistenceDirectory() {
        return (File) getParameterValue(BUFFER_DIRECTORY);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) {
        return similarity(datasetLoader, user1.getId(), user2.getId());
    }
}
