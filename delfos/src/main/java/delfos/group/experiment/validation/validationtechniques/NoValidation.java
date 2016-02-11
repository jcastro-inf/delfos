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
