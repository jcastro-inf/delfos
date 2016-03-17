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

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Validación cruzada para sistemas de recomendación a grupos. Esta validación
 * elimina cierto porcentaje de items verticalmente. No tiene en cuenta nada más
 * que el conjunto de productos existentes en el dataset de contenido.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class CrossFoldValidation_Items extends GroupValidationTechnique {

    /**
     * Número de particiones.
     */
    public static final Parameter NUMBER_OF_PARTITIONS = new Parameter(
            "NUMBER_OF_PARTITIONS",
            new IntegerParameter(1, Integer.MAX_VALUE, 5),
            "Número de particiones.");

    public CrossFoldValidation_Items() {
        super();
        addParameter(NUMBER_OF_PARTITIONS);
    }

    public CrossFoldValidation_Items(long seed) {
        this();
        setSeedValue(seed);
    }

    /**
     * {@inheritDoc }
     *
     * @return Número de particiones que realiza esta validación.
     */
    @Override
    public final int getNumberOfSplits() {
        return (Integer) getParameterValue(NUMBER_OF_PARTITIONS);
    }

    /**
     * {@inheritDoc }
     *
     * @param datasetLoader Dataset de entrada.
     * @return Pares de conjuntos de training y test. Este vector tendrá {@link GroupValidationTechnique#getNumberOfSplits()
     * }.
     * @throws IllegalArgumentException Cuando se especifica un conjunto de
     * grupos que compartan usuarios, es decir, un mismo usuario está en
     * distintos grupos.
     */
    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader, Iterable<GroupOfUsers> groupsOfUsers) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        checkDatasetLoaderNotNull(datasetLoader);

        checkGroupsAreNotSharingUsers(groupsOfUsers);

        Random random = new Random(getSeedValue());

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[getNumberOfSplits()];

        Set<Integer> allItems = new TreeSet<>(datasetLoader.getContentDataset().allIDs());

        List<Map<Integer, Set<Integer>>> finalTestSets = new ArrayList<>(getNumberOfSplits());

        List<Set<Integer>> itemsTestSets = new ArrayList<>(getNumberOfSplits());

        for (int i = 0; i < getNumberOfSplits(); i++) {
            finalTestSets.add(new TreeMap<>());
            itemsTestSets.add(new TreeSet<>());
        }

        {
            //Hago la partición de los productos general, sin tener en cuenta valoraciones.
            Set<Integer> allItems_sub = new TreeSet<>(allItems);
            int partition = 0;
            while (!allItems_sub.isEmpty()) {

                int idItem = allItems_sub.toArray(new Integer[0])[random.nextInt(allItems_sub.size())];

                allItems_sub.remove(idItem);

                itemsTestSets.get(partition % getNumberOfSplits()).add(idItem);
                partition++;

            }
        }

        {
            //Construyo los conjuntos de test para cada usuario.
            for (int idPartition = 0; idPartition < getNumberOfSplits(); idPartition++) {
                Set<Integer> testItems = itemsTestSets.get(idPartition);
                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage(testItems + "\n");
                }

                for (GroupOfUsers group : groupsOfUsers) {
                    for (int idUser : group) {
                        Set<Integer> itemsTest_user;
                        try {
                            itemsTest_user = new TreeSet<>(datasetLoader.getRatingsDataset().getUserRated(idUser));
                            itemsTest_user.retainAll(testItems);
                            finalTestSets.get(idPartition).put(idUser, itemsTest_user);
                        } catch (UserNotFound ex) {
                            ERROR_CODES.USER_NOT_FOUND.exit(ex);
                        }
                    }
                }
            }
        }
        for (int idPartition = 0; idPartition < getNumberOfSplits(); idPartition++) {
            try {

                ret[idPartition] = new PairOfTrainTestRatingsDataset(
                        datasetLoader,
                        ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(),
                                finalTestSets.get(idPartition)),
                        ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), finalTestSets.get(idPartition)),
                        "_" + this.getClass().getSimpleName() + "_seed=" + getSeedValue() + "_partition=" + idPartition
                );

            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }

        }
        return ret;
    }
}
