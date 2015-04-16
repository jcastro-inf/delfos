package delfos.experiment.validation.predictionprotocol;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;

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
    public Collection<Collection<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Collection<Integer> userRated = testRatingsDataset.getUserRated(idUser);

        Collection<Collection<Integer>> ret = new LinkedList<Collection<Integer>>();

        for (int idItem : userRated) {
            List<Integer> lista = new LinkedList<Integer>();
            lista.add(idItem);
            ret.add(lista);
        }

        return ret;
    }
}
