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
import delfos.common.parameters.restriction.FloatParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.dataset.util.DatasetOperations;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Validación hold out para sistemas de recomendación a grupos. Esta validación
 * elimina cierto porcentaje de items valorados por el grupo. No tiene en cuenta
 * si los productos han sido valorados por un solo miembro o por varios, por lo
 * que el número de ratings que caen en el conjunto de test no coincide con el
 * porcentaje de test.
 *
 * Se elimina, de cada grupo, el TRAIN_PERCENT de items valorados.
 *
 * LIMITACIONES: Esta validación solo se puede aplicar si los grupos formados no
 * repiten un mismo usuario, es decir, un usuario sólo se encuentra en un grupo.
 * En caso de que se de esta situación, el método {@link HoldOut_Items#shuffle(java.util.Collection)
 * } informa de esta situación y termina sin realizar la validación.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 14-02-2013
 */
public class HoldOutGroupRatedItems extends GroupValidationTechnique {

    public static final Parameter TRAIN_PERCENT = new Parameter(
            "Training_percent",
            new FloatParameter(0, 1, 0.8f),
            "Porcentaje de valoraciones que contiene el conjunto de entrenamiento.");

    public HoldOutGroupRatedItems() {
        super();
        addParameter(TRAIN_PERCENT);
    }

    public HoldOutGroupRatedItems(long seed) {
        this();
        setSeedValue(seed);
    }

    @Override
    public int getNumberOfSplits() {
        return 1;
    }

    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(
            DatasetLoader<? extends Rating> datasetLoader,
            Iterable<GroupOfUsers> groupsOfUsers)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        checkDatasetLoaderNotNull(datasetLoader);
        checkGroupsOfUsersNotNull(groupsOfUsers);
        checkGroupsAreNotSharingUsers(groupsOfUsers);

        final long seed = getSeedValue();
        Random random = new Random(seed);

        Map<Integer, Set<Integer>> testSet = new TreeMap<>();

        final Set<Integer> allRatedItems_this = new TreeSet<>();

        for (GroupOfUsers groupOfUsers : groupsOfUsers) {

            Set<Integer> allRatedItems_thisGroup = getGroupRatedItems(datasetLoader, groupOfUsers);
            allRatedItems_this.addAll(allRatedItems_thisGroup);

            double testPercent = 1.0 - getTrainPercent();

            double numTestItemsDouble = allRatedItems_thisGroup.size() * testPercent;

            int numTestItems = (int) numTestItemsDouble;
            if (numTestItems == 0 && !allRatedItems_thisGroup.isEmpty()) {
                numTestItems = 1;
            }

            Set<Integer> testItems_group = new TreeSet<>();
            while (testItems_group.size() < numTestItems) {
                int indexRemove = random.nextInt(allRatedItems_thisGroup.size());
                int idItem = allRatedItems_thisGroup.toArray(new Integer[0])[indexRemove];
                allRatedItems_thisGroup.remove(idItem);
                testItems_group.add(idItem);
            }

            for (int idUser : groupOfUsers) {
                Set<Integer> itemsTest_user = new TreeSet<>(testItems_group);
                try {
                    itemsTest_user.retainAll(datasetLoader.getRatingsDataset().getUserRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }

                testSet.put(idUser, itemsTest_user);
            }

        }

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[getNumberOfSplits()];
        try {
            ret[0] = new PairOfTrainTestRatingsDataset(
                    datasetLoader,
                    ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), testSet),
                    ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), testSet));

            if (Global.isVerboseAnnoying()) {

                Set<Integer> allUsers = new TreeSet<>();
                for (GroupOfUsers g : groupsOfUsers) {
                    allUsers.addAll(g.getIdMembers());
                }

                Global.showInfoMessage("Dataset de training.\n");
                DatasetPrinterDeprecated.printCompactRatingTable(
                        ret[0].train,
                        allUsers,
                        allRatedItems_this);

                Global.showInfoMessage("Dataset de test.\n");
                DatasetPrinterDeprecated.printCompactRatingTable(
                        ret[0].test,
                        allUsers,
                        allRatedItems_this);
            }
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        }

        return ret;
    }

    public Set<Integer> getGroupRatedItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws CannotLoadRatingsDataset, IllegalArgumentException {
        //Para cada grupo, compruebo qué productos pueden ser candidatos a test
        Set<Integer> allRatedItems_thisGroup = new TreeSet<>();
        {
            for (int idUser : groupOfUsers) {
                try {
                    allRatedItems_thisGroup.addAll(datasetLoader.getRatingsDataset().getUserRated(idUser));
                } catch (UserNotFound ex) {
                    throw new IllegalArgumentException("The user '" + idUser + "' in group " + groupOfUsers + " is not in the rating dataset (User doesn't have ratings.");
                }
            }

            if (Global.isVerboseAnnoying()) {
                Map<Integer, Map<Integer, ? extends Rating>> groupRatings = new TreeMap<>();
                for (int idUser : groupOfUsers) {
                    try {
                        groupRatings.put(idUser, datasetLoader.getRatingsDataset().getUserRatingsRated(idUser));
                    } catch (UserNotFound ex) {
                        throw new IllegalArgumentException("The user '" + idUser + "' in group " + groupOfUsers + " is not in the rating dataset (User doesn't have ratings.");
                    }
                }
                Global.showInfoMessage("Ratings of group " + groupOfUsers + ".\n");

                DatasetPrinterDeprecated.printCompactRatingTable(DatasetOperations.convertRatingsToNumber(groupRatings), groupOfUsers.getIdMembers(), allRatedItems_thisGroup);
            }
        }
        return allRatedItems_thisGroup;
    }

    private float getTrainPercent() {
        return (Float) getParameterValue(TRAIN_PERCENT);
    }
}
