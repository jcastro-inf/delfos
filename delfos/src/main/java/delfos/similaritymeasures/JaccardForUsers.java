package delfos.similaritymeasures;

import java.util.Collection;
import java.util.TreeSet;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;

/**
 * Clase que implementa la medida Jaccard para la similitud de los usuarios.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 16-Julio-2013
 */
public class JaccardForUsers {

    public float similarity(RatingsDataset<? extends Rating> ratings, int idUser1, int idUser2) throws CouldNotComputeSimilarity {

        //Calculo el Jaccard
        Collection<Integer> user1Ratings;
        try {
            user1Ratings = ratings.getUserRated(idUser1);
        } catch (UserNotFound ex) {
            throw new CouldNotComputeSimilarity(ex);
        }

        Collection<Integer> user2Ratings;
        try {
            user2Ratings = ratings.getUserRated(idUser2);
        } catch (UserNotFound ex) {
            throw new CouldNotComputeSimilarity(ex);
        }

        TreeSet<Integer> intersection = new TreeSet<>(user1Ratings);
        intersection.retainAll(user2Ratings);

        TreeSet<Integer> union = new TreeSet<>(user1Ratings);
        union.addAll(user2Ratings);

        if (union.isEmpty()) {
            return 0;
        } else {

            float numCommon = intersection.size();

            float jaccard = numCommon / union.size();
            return jaccard;
        }
    }

    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return true;
    }
}
