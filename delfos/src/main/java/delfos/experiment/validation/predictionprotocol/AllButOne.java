package delfos.experiment.validation.predictionprotocol;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Esta técnica realiza la composición de recomendaciones de manera que se
 * utilizan todos los ratings del usuario en la predicción excepto el que se
 * desea predecir.
 *
 * @author Jorge Castro Gallardo
 */
public class AllButOne extends PredictionProtocol {

    private static final long serialVersionUID = 1L;

    @Override
    public Collection<Set<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Collection<Integer> userRated = testRatingsDataset.getUserRated(idUser);

        Collection<Set<Integer>> collectionOfSetsOfRequests = new LinkedList<>();

        for (int idItem : userRated) {
            Set<Integer> oneRequestSet = new TreeSet<>();
            oneRequestSet.add(idItem);
            collectionOfSetsOfRequests.add(oneRequestSet);
        }

        return collectionOfSetsOfRequests;
    }
}
