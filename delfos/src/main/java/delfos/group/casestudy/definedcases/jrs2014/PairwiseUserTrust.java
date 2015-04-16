package delfos.group.casestudy.definedcases.jrs2014;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 24-feb-2014
 */
public interface PairwiseUserTrust {

    /**
     * Devuelve la confianza entre dos usuarios, usando
     *
     * @param datasetLoader
     * @param idUser1
     * @param idUser2
     * @return
     * @throws delfos.common.Exceptions.Dataset.Users.UserNotFound
     * @throws
     * delfos.group.CaseStudy.DefinedCases.JRS2014.CouldNotComputeTrust
     */
    double getTrust(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) throws UserNotFound, CouldNotComputeTrust;

}
