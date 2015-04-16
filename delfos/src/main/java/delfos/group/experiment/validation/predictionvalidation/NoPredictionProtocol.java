package delfos.group.experiment.validation.predictionvalidation;

import java.util.Arrays;
import java.util.Collection;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Implementa el protocolo de predicción HoldOut, que divide el conjunto de
 * productos valorados por todos los usuarios en dos conjuntos disjuntos:
 * entrenamiento y evaluación. Lo hace de manera que un cierto porcentaje de
 * productos estén en el conjunto de entrenamiento, y el resto en el de
 * evaluación
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (10-12-2012)
 */
public class NoPredictionProtocol extends GroupPredictionProtocol {

    public NoPredictionProtocol() {
        super();
    }

    private Collection<Integer> getRatedItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {
        return DatasetUtilities.getMembersRatings_byItem(group, datasetLoader).keySet();
    }

    @Override
    public Collection<GroupRecommendationRequest> getGroupRecommendationRequests(
            DatasetLoader<? extends Rating> trainDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {
        return Arrays.asList(new GroupRecommendationRequest(
                group,
                trainDatasetLoader,
                getRatedItems(testDatasetLoader, group)));

    }
}
