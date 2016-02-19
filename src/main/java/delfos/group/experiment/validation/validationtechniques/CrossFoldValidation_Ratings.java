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
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.dataset.util.DatasetPrinterDeprecated;
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
 * elimina ciertas valoraciones de todos los usuarios de un grupo, destinadas a
 * los item que determine el conjunto de test.
 *
 * Esta validación solo se puede aplicar si los grupos formados no repiten un
 * mismo usuario, es decir, un usuario sólo se encuentra en un grupo. En caso de
 * que se de esta situación, el método {@link CrossFoldValidation_Ratings#shuffle(java.util.Collection)
 * } informa de esta situación y termina sin realizar la validación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 10-Julio-2013
 */
public class CrossFoldValidation_Ratings extends GroupValidationTechnique {

    /**
     * Número de particiones.
     */
    public static final Parameter NUMBER_OF_PARTITIONS = new Parameter(
            "NUMBER_OF_PARTITIONS",
            new IntegerParameter(1, Integer.MAX_VALUE, 5),
            "Número de particiones.");

    public CrossFoldValidation_Ratings() {
        super();
        addParameter(NUMBER_OF_PARTITIONS);
    }

    public CrossFoldValidation_Ratings(long seed) {
        this();
        setSeedValue(seed);
    }

    /**
     * {@inheritDoc }
     *
     * @return
     */
    @Override
    public final int getNumberOfSplits() {
        return (Integer) getParameterValue(NUMBER_OF_PARTITIONS);
    }

    /**
     * {@inheritDoc }
     *
     * @return
     * @throws IllegalArgumentException Cuando se especifica un conjunto de
     * grupos que compartan usuarios, es decir, un mismo usuario está en
     * distintos grupos.
     */
    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader, Iterable<GroupOfUsers> groupsOfUsers) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        if (datasetLoader == null) {
            throw new IllegalArgumentException("DatasetLoader<? extends Rating> is null.");
        }

        if (groupsOfUsers == null) {
            throw new IllegalArgumentException("The parameter 'groupOfUsers' is null.");
        }

        Random random = new Random(getSeedValue());

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[getNumberOfSplits()];

        {
            //Compruebo que cada usuario está únicamente en un grupo.
            Set<Integer> users = new TreeSet<>();
            int numUsersInGroups = 0;

            for (GroupOfUsers g : groupsOfUsers) {
                users.addAll(g.getIdMembers());
                numUsersInGroups += g.size();
            }

            if (users.size() != numUsersInGroups) {
                throw new IllegalArgumentException("Groups are sharing users, can't perform this validation.");
            }
        }

        List<Map<Integer, Set<Integer>>> finalTestSets = new ArrayList<>(getNumberOfSplits());

        for (int i = 0; i < getNumberOfSplits(); i++) {
            finalTestSets.add(new TreeMap<>());
        }

        {
            for (GroupOfUsers groupOfUsers : groupsOfUsers) {
                //Para cada grupo, calculo sus 5 particiones.
                Set<Integer> itemsValoradosGrupo = new TreeSet<>();

                Map<Integer, Map<Integer, ? extends Rating>> groupRatings = new TreeMap<>();
                for (int idUser : groupOfUsers) {
                    try {
                        groupRatings.put(idUser, datasetLoader.getRatingsDataset().getUserRatingsRated(idUser));
                        itemsValoradosGrupo.addAll(groupRatings.get(idUser).keySet());
                    } catch (UserNotFound ex) {
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                        throw new IllegalArgumentException(ex);
                    }
                }

                int i = 0;
                while (!itemsValoradosGrupo.isEmpty()) {

                    int particion = i % getNumberOfSplits();
                    int indexRemove = random.nextInt(itemsValoradosGrupo.size());
                    int idItem = itemsValoradosGrupo.toArray(new Integer[0])[indexRemove];

                    itemsValoradosGrupo.remove(idItem);

                    for (int idMember : groupOfUsers) {
                        if (!finalTestSets.get(particion).containsKey(idMember)) {
                            finalTestSets.get(particion).put(idMember, new TreeSet<>());
                        }

                        if (groupRatings.get(idMember).containsKey(idItem)) {
                            finalTestSets.get(particion).get(idMember).add(idItem);
                        }
                    }
                    i++;
                }
            }
        }
        for (int idPartition = 0; idPartition < getNumberOfSplits(); idPartition++) {
            try {

                ret[idPartition] = new PairOfTrainTestRatingsDataset(
                        datasetLoader,
                        ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), finalTestSets.get(idPartition)),
                        ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), finalTestSets.get(idPartition)),
                        "_" + this.getClass().getSimpleName() + "_seed=" + getSeedValue() + "_partition=" + idPartition);

                if (Global.isVerboseAnnoying()) {

                    Set<Integer> allItems;
                    if (datasetLoader instanceof ContentDatasetLoader) {
                        ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
                        allItems = new TreeSet<>(contentDatasetLoader.getContentDataset().allIDs());
                    } else {
                        allItems = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());
                    }

                    Global.showInfoMessage("==================================================== \n\n");

                    Set<Integer> allUsers = new TreeSet<>(datasetLoader.getRatingsDataset().allUsers());

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
