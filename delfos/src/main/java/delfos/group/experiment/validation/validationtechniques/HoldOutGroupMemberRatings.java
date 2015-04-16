package delfos.group.experiment.validation.validationtechniques;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Validación hold out para sistemas de recomendación a grupos. Esta validación
 * elimina cierto porcentaje de ratings de todos los miembros del grupo,
 * eliminando para todos ellos el mismo porcentaje.
 *
 * Provoca situaciones en las que dos usuarios hayan valorado el mismo producto
 * y uno de los ratings esté en el conjunto de entrenamiento mientras que el
 * otro esté en el conjunto de test. Para resolver esto, quizas habría que
 * permitir la recomendación de productos ya valorados, pidiendo al sistema de
 * recomendación que prediga todos los items con al menos un rating en el
 * conjunto de test.
 *
 * BENEFICIOS: Esta validación se puede aplicar incluso si los grupos formados
 * comparten usuarios, es decir, un usuario está en más de un grupo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 17-02-2015
 */
public class HoldOutGroupMemberRatings extends GroupValidationTechnique {

    public static final Parameter TRAIN_PERCENT = new Parameter(
            "Training_percent",
            new FloatParameter(0, 1, 0.8f),
            "Porcentaje de valoraciones que contiene el conjunto de entrenamiento.");

    public HoldOutGroupMemberRatings() {
        super();
        addParameter(TRAIN_PERCENT);
    }

    public HoldOutGroupMemberRatings(long seed) {
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

        if (datasetLoader == null) {
            throw new IllegalArgumentException("The datasetLoader is null.");
        }

        if (groupsOfUsers == null) {
            throw new IllegalArgumentException("The parameter 'groupOfUsers' is null.");
        }
        final long seed = getSeedValue();
        Random random = new Random(seed);

        final double testPercent = 1.0 - getTrainPercent();

        Set<Integer> usersInGroups = getUsersInGroups(groupsOfUsers);

        Map<Integer, Set<Integer>> testSet = new TreeMap<>();

        for (Integer idUser : usersInGroups) {
            try {
                TreeSet<Integer> itemsRatedByThisUser = new TreeSet<>(datasetLoader.getRatingsDataset().getUserRated(idUser));

                int numTestItems = (int) (itemsRatedByThisUser.size() * testPercent);

                if (itemsRatedByThisUser.isEmpty()) {
                    throw new IllegalStateException("Cannot use the user '" + idUser + "', (s)he has no ratigns");
                }

                Set<Integer> testItemsThisUser = new TreeSet<>();
                while (testItemsThisUser.size() < numTestItems) {
                    int indexRemove = random.nextInt(itemsRatedByThisUser.size());
                    int idItem = itemsRatedByThisUser.toArray(new Integer[0])[indexRemove];
                    itemsRatedByThisUser.remove(idItem);
                    testItemsThisUser.add(idItem);
                }

                testSet.put(idUser, testItemsThisUser);
            } catch (UserNotFound ex) {
                Logger.getLogger(HoldOutGroupMemberRatings.class.getName()).log(Level.SEVERE, null, ex);
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
                    allUsers.addAll(g.getGroupMembers());
                }

                Global.showMessage("Dataset de training.\n");
                String printTrainingSet = DatasetPrinter.printCompactRatingTable(
                        ret[0].train,
                        allUsers,
                        datasetLoader.getRatingsDataset().allRatedItems());

                Global.showMessage(printTrainingSet);

                Global.showMessage("Dataset de test.\n");
                String printTestSet = DatasetPrinter.printCompactRatingTable(
                        ret[0].test,
                        allUsers,
                        datasetLoader.getRatingsDataset().allRatedItems());
                Global.showMessage(printTestSet);
            }
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        }

        return ret;
    }

    private Set<Integer> getUsersInGroups(Iterable<GroupOfUsers> groupsOfUsers) {
        Set<Integer> usersInGroups = new TreeSet<>();
        for (GroupOfUsers groupOfUsers : groupsOfUsers) {
            usersInGroups.addAll(groupOfUsers.getGroupMembers());
        }
        return usersInGroups;
    }

    private float getTrainPercent() {
        return (Float) getParameterValue(TRAIN_PERCENT);
    }
}
