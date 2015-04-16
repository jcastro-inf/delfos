package delfos.experiment.validation.validationtechnique;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;

/**
 * Clase que implementa el método validación todos menos 1. Solo tiene sentido
 * aplicar este método de validación en el sistema {@link KnnMemoryBasedCFRS}
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 (19 Octubre 2011)
 */
public class NoPartitions extends ValidationTechnique {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor de la clase que genera los conjuntos de validación. actual
     * {@link System#currentTimeMillis()}
     */
    public NoPartitions() {
    }

    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadContentDataset, CannotLoadRatingsDataset {
        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[1];
        ret[0] = new PairOfTrainTestRatingsDataset(datasetLoader, datasetLoader.getRatingsDataset(), datasetLoader.getRatingsDataset());
        return ret;
    }

    @Override
    public int getNumberOfSplits() {
        return 1;
    }
}
