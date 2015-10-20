package delfos.group.casestudy.definedcases.jrs2014;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Calcula la confianza entre dos usuarios.
 *
* @author Jorge Castro Gallardo
 *
 * @version 24-feb-2014
 */
public class PearsonWithPenalty implements PairwiseUserTrust {

    private final int penalty;

    public PearsonWithPenalty(int penalty) {
        this.penalty = penalty;

    }

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
    public double getTrust(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) throws UserNotFound, CouldNotComputeTrust {

        Map<Integer, ? extends Rating> user1ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
        Map<Integer, ? extends Rating> user2ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

        Set<Integer> commonItems = new TreeSet<Integer>(user1ratings.keySet());
        commonItems.retainAll(user2ratings.keySet());

        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();
        ArrayList<Float> user1 = new ArrayList<Float>(commonItems.size());
        ArrayList<Float> user2 = new ArrayList<Float>(commonItems.size());
        for (int idItem : commonItems) {
            user1.add(user1ratings.get(idItem).getRatingValue().floatValue());
            user2.add(user2ratings.get(idItem).getRatingValue().floatValue());
        }
        double similarity;
        try {
            similarity = pcc.similarity(user1, user2);
            similarity = (similarity + 1) / 2;
        } catch (CouldNotComputeSimilarity ex) {
            throw new CouldNotComputeTrust(ex);
        }
        if (commonItems.size() < penalty) {
            similarity = commonItems.size() * (similarity / penalty);
        }

        if (similarity < 0) {
            similarity = 0;
        }

        if (similarity > 1) {
            similarity = 1;
        }
        return similarity;
    }
}
