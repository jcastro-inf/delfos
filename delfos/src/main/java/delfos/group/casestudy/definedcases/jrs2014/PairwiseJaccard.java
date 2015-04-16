package delfos.group.casestudy.definedcases.jrs2014;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Calcula la confianza entre dos usuarios.
 *
* @author Jorge Castro Gallardo
 *
 * @version 24-feb-2014
 */
public class PairwiseJaccard implements PairwiseUserTrust {

    /**
     * Devuelve la confianza entre dos usuarios, usando
     *
     * @param datasetLoader
     * @param idUser1
     * @param idUser2
     * @return
     * @throws delfos.common.Exceptions.Dataset.Users.UserNotFound
     */
    @Override
    public double getTrust(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) throws UserNotFound {

        double Jaccard;

        {

            Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
            Map<Integer, ? extends Rating> userNeighbourRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

            Set<Integer> commonItems = new TreeSet<Integer>(userRatings.keySet());
            commonItems.retainAll(userNeighbourRatings.keySet());

            //Calculo el Jaccard
            double numUserRatings = userRatings.size();
            double numUserNeighborRatings = userNeighbourRatings.size();
            double numCommon = commonItems.size();
            double jaccard_of_pair = numCommon / (numUserRatings + numUserNeighborRatings - commonItems.size());
            Jaccard = jaccard_of_pair;
        }

        double ret = Jaccard;
        return ret;
    }
}
