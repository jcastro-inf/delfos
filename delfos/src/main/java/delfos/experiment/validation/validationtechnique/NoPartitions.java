/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
