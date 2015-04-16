package delfos.group.experiment.validation.validationtechniques;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 *
 * Validación nula. El conjunto de entrenamiento y el de evaluación son
 * exactamente iguales al conjunto original.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 14-Feb-2013
 */
public class NoValidation extends GroupValidationTechnique {

    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader, Iterable<GroupOfUsers> groupsOfUsers) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[1];
        ret[0] = new PairOfTrainTestRatingsDataset(datasetLoader, datasetLoader.getRatingsDataset(), datasetLoader.getRatingsDataset());
        return ret;
    }

    @Override
    public int getNumberOfSplits() {
        return 1;
    }
}
