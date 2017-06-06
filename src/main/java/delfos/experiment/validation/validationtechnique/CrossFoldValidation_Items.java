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

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.generated.modifieddatasets.SelectionDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Clase que implementa el método validación cross fold validation que se aplica en productos (las particiones las hace
 * por productos, no por ratings o por usuarios)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (19 Octubre 2011)
 */
public class CrossFoldValidation_Items extends ValidationTechnique {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para especificar el número de particiones que se realizan.
     */
    public static final Parameter numFolds = new Parameter("numFolds", new IntegerParameter(2, Integer.MAX_VALUE, 5));

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada. Por defecto tiene cinco particiones.
     */
    public CrossFoldValidation_Items() {
        addParameter(numFolds);
    }

    @Override
    public <RatingType extends Rating> PairOfTrainTestRatingsDataset<RatingType>[] shuffle(DatasetLoader<RatingType> datasetLoader) throws CannotLoadContentDataset, CannotLoadRatingsDataset {
        Random random = new Random(getSeedValue());

        int numSplits = getNumberOfFolds();
        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[numSplits];

        List<Long> setItems = new ArrayList<>(datasetLoader.getRatingsDataset().allRatedItems());
        Collection<Long>[] itemsTest = (Collection<Long>[]) new Collection[numSplits];
        for (int i = 0; i < numSplits; i++) {
            itemsTest[i] = new TreeSet<>();
        }
        while (!setItems.isEmpty()) {
            int index = 0;
            while (!setItems.isEmpty() && index < numSplits) {
                long idUser = setItems.remove(random.nextInt(setItems.size()));
                itemsTest[index].add(idUser);
                index++;
            }

        }
        for (Iterator<Long> it = setItems.listIterator(); it.hasNext();) {
            Long integer = it.next();
            itemsTest[random.nextInt(numSplits)].add(integer);
        }

        Set<Long> allUsers = new TreeSet<>(datasetLoader.getRatingsDataset().allUsers());

        Global.showInfoMessage("Original dataset #users " + datasetLoader.getRatingsDataset().allUsers().size() + "\n");
        for (int idPartition = 0; idPartition < numSplits; idPartition++) {
            Set<Long> productosEnTraining = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());
            productosEnTraining.removeAll(itemsTest[idPartition]);

            SelectionDataset training = new SelectionDataset(datasetLoader.getRatingsDataset());
            training.setAllowedItems(productosEnTraining);
            training.setAllowedUsers(allUsers);

            SelectionDataset test = new SelectionDataset(datasetLoader.getRatingsDataset());
            test.setAllowedItems(itemsTest[idPartition]);
            test.setAllowedUsers(allUsers);
            ret[idPartition] = new PairOfTrainTestRatingsDataset(datasetLoader, training, test,
                    "_" + this.getClass().getSimpleName() + "_seed=" + getSeedValue() + "_partition=" + idPartition);
            Global.showInfoMessage("------------------  " + idPartition + "  ------------------\n");
            Global.showInfoMessage("Training dataset #users " + training.allUsers().size() + "\n");
            Global.showInfoMessage("Test dataset #users     " + test.allUsers().size() + "\n");
        }
        progressChanged("Validation.shuffle() finished", 100);
        return ret;
    }

    public int getNumberOfFolds() {
        return ((Number) getParameterValue(numFolds)).intValue();
    }

    @Override
    public int getNumberOfSplits() {
        return getNumberOfFolds();
    }
}
