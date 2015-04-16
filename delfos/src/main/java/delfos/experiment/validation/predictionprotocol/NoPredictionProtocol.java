package delfos.experiment.validation.predictionprotocol;

import java.util.ArrayList;
import java.util.Collection;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;

/**
 * Protocolo de predicción nulo, es decir, no hace nada. Solo solicita todas las
 * valoraciones que están en el conjunto de test de una vez.
 *
 * Se utiliza cuando no se desea aplicar porotocolo de predicción, por ejemplo,
 * en el sistema de recomendación SVD {@link TryThisAtHomeSVD} no tiene sentido
 * aplicar protocolo de predicción, ya que cuando cambian las valoraciones del
 * usuario se debe volver a construir el modelo para que se actualicen las
 * recomendaciones.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 21-Jan-2013
 */
public class NoPredictionProtocol extends PredictionProtocol {

    public static final long serialVersionUID = 1L;

    @Override
    public Collection<Collection<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Collection<Collection<Integer>> listOfRequests = new ArrayList<>(1);

        Collection<Integer> userRated = new ArrayList<>(testRatingsDataset.getUserRated(idUser));

        listOfRequests.add(userRated);

        return listOfRequests;
    }
}
