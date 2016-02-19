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
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Clase que implementa el método validación cross fold validation que se aplica
 * en usuarios (las particiones las hace por usuarios, no por ratings o por
 * items) con la predicción todos menos 1 rating. {@link KnnMemoryBasedCFRS}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (19 Octubre 2011)
 */
public class CrossFoldValidation_Users extends ValidationTechnique {

    private static final long serialVersionUID = 1L;

    /**
     * Parámetro para almacenar el número de particiones que se realizan sobre
     * el dataset original.
     */
    public static final Parameter NUM_PARTITIONS = new Parameter("NUM_PARTITIONS", new IntegerParameter(2, Integer.MAX_VALUE, 5), "Número de particiones que se realizan sobre el dataset original.");

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada.
     * Por defecto tiene cinco particiones y la semilla utilizada será la fecha
     * actual {@link System#currentTimeMillis()}
     */
    public CrossFoldValidation_Users() {
        addParameter(NUM_PARTITIONS);
    }

    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        Random randomGenerator = new Random(getSeedValue());

        int numSplits = getNumberOfPartitions();

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[numSplits];

        List<Integer> setUsers = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());
        Collection<Integer>[] usersTest = (Collection<Integer>[]) new Collection[numSplits];
        for (int i = 0; i < numSplits; i++) {
            usersTest[i] = new TreeSet<>();
        }
        while (!setUsers.isEmpty()) {
            int index = 0;
            while (!setUsers.isEmpty() && index < numSplits) {
                int idUser = setUsers.remove(randomGenerator.nextInt(setUsers.size()));
                usersTest[index].add(idUser);
                index++;
            }

        }
        for (Iterator<Integer> it = setUsers.listIterator(); it.hasNext();) {
            Integer integer = it.next();
            usersTest[randomGenerator.nextInt(numSplits)].add(integer);
        }

        Set<Integer> allItems = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());

        Global.showInfoMessage("Original dataset #users " + datasetLoader.getRatingsDataset().allUsers().size() + "\n");
        for (int idPartition = 0; idPartition < numSplits; idPartition++) {
            Set<Integer> usuariosEnTraining = new TreeSet<>(datasetLoader.getRatingsDataset().allUsers());
            usuariosEnTraining.removeAll(usersTest[idPartition]);

            SelectionDataset training = new SelectionDataset(datasetLoader.getRatingsDataset());
            training.setProductosPermitidos(allItems);
            training.setUsuariosPermitidos(usuariosEnTraining);

            SelectionDataset test = new SelectionDataset(datasetLoader.getRatingsDataset());
            test.setProductosPermitidos(allItems);
            test.setUsuariosPermitidos(usersTest[idPartition]);

            ret[idPartition] = new PairOfTrainTestRatingsDataset(datasetLoader, training, test,
                    "_" + this.getClass().getSimpleName() + "_seed=" + getSeedValue() + "_partition=" + idPartition);

            Global.showInfoMessage("------------------  " + idPartition + "  ------------------\n");
            Global.showInfoMessage("Training dataset #users " + training.allUsers().size() + "\n");
            Global.showInfoMessage("Test dataset #users     " + test.allUsers().size() + "\n");
        }
        progressChanged("Validation.shuffle() finished", 100);
        return ret;
    }

    public int getNumberOfPartitions() {
        return ((Number) getParameterValue(NUM_PARTITIONS)).intValue();
    }

    @Override
    public int getNumberOfSplits() {
        return getNumberOfPartitions();
    }
}
