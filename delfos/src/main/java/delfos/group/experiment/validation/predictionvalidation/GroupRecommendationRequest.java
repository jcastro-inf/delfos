package delfos.group.experiment.validation.predictionvalidation;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Set;

/**
 * Encapsula los resultados del protocolo de predicción para grupos.
 *
 * @author jcastro
 */
public class GroupRecommendationRequest {

    public final DatasetLoader<? extends Rating> predictionPhaseDatasetLoader;
    public final GroupOfUsers groupOfUsers;
    public final Set<Integer> itemsToPredict;

    public GroupRecommendationRequest(
            GroupOfUsers groupOfUsers,
            DatasetLoader<? extends Rating> predictionPhaseDatasetLoader,
            Set<Integer> itemsToPredict) {

        this.groupOfUsers = groupOfUsers;
        this.predictionPhaseDatasetLoader = predictionPhaseDatasetLoader;
        this.itemsToPredict = itemsToPredict;

    }

}
