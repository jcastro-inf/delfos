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
import delfos.dataset.basic.user.User;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Validación cruzada para sistemas de recomendación a grupos. Esta validación
 * elimina cierto porcentaje de items valorados por el grupo. No tiene en cuenta
 * si los productos han sido valorados por un solo miembro o por varios, por lo
 * que el número de ratings que caen en el conjunto de test no coincide con el
 * porcentaje de test.
 *
 * Esta validación solo se puede aplicar si los grupos formados no repiten un
 * mismo usuario, es decir, un usuario sólo se encuentra en un grupo. En caso de
 * que se de esta situación, el método {@link CrossFoldValidation_Items#shuffle(java.util.Collection)
 * } informa de esta situación y termina sin realizar la validación.
 *
 * @see HoldOutGroupRatedItems
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class CrossFoldValidation_groupRatedItems extends GroupValidationTechnique {

    /**
     * Número de particiones.
     */
    public static final Parameter NUMBER_OF_PARTITIONS = new Parameter(
            "NUMBER_OF_PARTITIONS",
            new IntegerParameter(1, Integer.MAX_VALUE, 5),
            "Número de particiones.");

    public CrossFoldValidation_groupRatedItems() {
        super();
        addParameter(NUMBER_OF_PARTITIONS);
    }

    public CrossFoldValidation_groupRatedItems(long seed) {
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
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader, Iterable<GroupOfUsers> groupsOfUsersIterable) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        checkGroupsAreNotSharingUsers(groupsOfUsersIterable);
        checkDatasetLoaderNotNull(datasetLoader);
        checkGroupsAreNotSharingUsers(groupsOfUsersIterable);

        List<GroupOfUsers> groupsOfUsers = new ArrayList<>();
        for (Iterator<GroupOfUsers> iterator = groupsOfUsersIterable.iterator(); iterator.hasNext();) {
            GroupOfUsers groupOfUsers = iterator.next();
            groupsOfUsers.add(groupOfUsers);
        }
        Collections.sort(groupsOfUsers, GroupOfUsers.BY_MEMBERS_ID);

        Random random = new Random(getSeedValue());

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[getNumberOfSplits()];

        Set<Integer> allItems = new TreeSet<>(datasetLoader.getContentDataset().allIDs());

        Map<GroupOfUsers, List<Set<Integer>>> partitionsByGroup = new TreeMap<>();

        for (GroupOfUsers groupOfUsers : groupsOfUsers) {
            partitionsByGroup.put(groupOfUsers, new ArrayList<>(getNumberOfSplits()));

            for (int i = 0; i < getNumberOfSplits(); i++) {
                partitionsByGroup.get(groupOfUsers).add(new TreeSet<>());

            }
        }

        //Hago las particiones para cada grupo.
        for (GroupOfUsers groupOfUsers : groupsOfUsers) {
            List<Integer> groupRatedItems = DatasetUtilities.getMembersRatings_byItem(groupOfUsers, datasetLoader).keySet().stream().collect(Collectors.toList());

            int partition = 0;
            while (!groupRatedItems.isEmpty()) {
                Integer item = groupRatedItems.remove(random.nextInt(groupRatedItems.size()));
                partitionsByGroup.get(groupOfUsers).get(partition).add(item);
                partition = (partition + 1) % getNumberOfSplits();
            }
        }

        List<Map<Integer, Set<Integer>>> finalTestSets = new ArrayList<>(getNumberOfSplits());
        for (int i = 0; i < getNumberOfSplits(); i++) {
            finalTestSets.add(new TreeMap<>());
        }

        for (GroupOfUsers groupOfUsers : groupsOfUsers) {
            List<Set<Integer>> thisGroupPartitions = partitionsByGroup.get(groupOfUsers);
            for (User user : groupOfUsers.getMembers()) {
                for (int partition = 0; partition < getNumberOfSplits(); partition++) {
                    Set<Integer> testItemsForThisMemberOnThisSplit = new TreeSet<>(datasetLoader.getRatingsDataset().getUserRated(user.getId()));
                    testItemsForThisMemberOnThisSplit.retainAll(thisGroupPartitions.get(partition));
                    finalTestSets.get(partition).put(user.getId(), testItemsForThisMemberOnThisSplit);
                }
            }
        }

        for (int idPartition = 0; idPartition < getNumberOfSplits(); idPartition++) {
            try {

                ret[idPartition] = new PairOfTrainTestRatingsDataset(
                        datasetLoader,
                        ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(),
                                finalTestSets.get(idPartition)),
                        ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), finalTestSets.get(idPartition)));

                if (Global.isVerboseAnnoying()) {

                    Global.showInfoMessage("==================================================== \n");

                    Set<Integer> allUsers = new TreeSet<>();
                    for (GroupOfUsers g : groupsOfUsers) {
                        allUsers.addAll(g.getIdMembers());
                    }

                    Global.showInfoMessage("Dataset de training " + idPartition + ".\n");
                    DatasetPrinterDeprecated.printCompactRatingTable(
                            ret[idPartition].train,
                            allUsers,
                            allItems);

                    Global.showInfoMessage("Dataset de test " + idPartition + ".\n");
                    DatasetPrinterDeprecated.printCompactRatingTable(
                            ret[idPartition].test,
                            allUsers,
                            allItems);
                    Global.showInfoMessage("==================================================== \n");
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }

        }
        return ret;
    }
}
