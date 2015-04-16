package delfos.group.experiment.validation.predictionvalidation;

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Encapsula los resultados del protocolo de predicción para grupos.
 *
 * @author jcastro
 */
public class GroupRecommendationRequest {

    public final DatasetLoader<? extends Rating> predictionPhaseDatasetLoader;
    public final GroupOfUsers groupOfUsers;
    public final Collection<Integer> itemsToPredict;

    public GroupRecommendationRequest(
            GroupOfUsers groupOfUsers,
            DatasetLoader<? extends Rating> predictionPhaseDatasetLoader,
            Collection<Integer> itemsToPredict) {

        this.groupOfUsers = groupOfUsers;
        this.predictionPhaseDatasetLoader = predictionPhaseDatasetLoader;
        this.itemsToPredict = itemsToPredict;

    }

}
