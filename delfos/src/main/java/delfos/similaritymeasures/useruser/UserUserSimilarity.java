package delfos.similaritymeasures.useruser;

import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.similaritymeasures.SimilarityMeasure;

/**
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public interface UserUserSimilarity extends SimilarityMeasure {

    /**
     * Similitud entre dos usuarios, utilizando los datos del datasetLoader.
     *
     * @param datasetLoader
     * @param idUser1
     * @param idUser2
     * @return
     */
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) throws UserNotFound, CouldNotComputeSimilarity;
}
