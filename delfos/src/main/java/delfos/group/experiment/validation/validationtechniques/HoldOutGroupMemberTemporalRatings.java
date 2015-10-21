package delfos.group.experiment.validation.validationtechniques;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
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
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Validación hold out para sistemas de recomendación a grupos. Esta validación
 * elimina cierto porcentaje de ratings de todos los miembros del grupo,
 * eliminando para todos ellos el mismo porcentaje. Elimina los más nuevos.
 *
 * BENEFICIOS: Esta validación se puede aplicar incluso si los grupos formados
 * comparten usuarios, es decir, un usuario está en más de un grupo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 18-feb-2015
 */
public class HoldOutGroupMemberTemporalRatings extends GroupValidationTechnique {

    public static final Parameter TRAIN_PERCENT = new Parameter(
            "Training_percent",
            new FloatParameter(0, 1, 0.8f),
            "Porcentaje de valoraciones que contiene el conjunto de entrenamiento.");

    public HoldOutGroupMemberTemporalRatings() {
        super();
        addParameter(TRAIN_PERCENT);
    }

    public HoldOutGroupMemberTemporalRatings(long seed) {
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

        checkParameterNotNull(datasetLoader, groupsOfUsers);
        checkRatingsWithTimestamp(datasetLoader.getRatingsDataset());

        final double trainPercent = getTrainPercent();

        Set<Integer> usersInGroups = getUsersInGroups(groupsOfUsers);

        Map<Integer, Set<Integer>> testSet = new TreeMap<>();

        for (Integer idUser : usersInGroups) {
            try {
                ArrayList<RatingWithTimestamp> thisUserRatingsWithTimestamp = new ArrayList<>();

                for (Rating rating : datasetLoader.getRatingsDataset().getUserRatingsRated(idUser).values()) {
                    thisUserRatingsWithTimestamp.add((RatingWithTimestamp) rating);
                }

                int firstTestItem = (int) (thisUserRatingsWithTimestamp.size() * trainPercent);

                if (thisUserRatingsWithTimestamp.isEmpty()) {
                    throw new IllegalStateException("Cannot use the user '" + idUser + "', (s)he has no ratigns");
                }

                Collections.sort(thisUserRatingsWithTimestamp, (RatingWithTimestamp o1, RatingWithTimestamp o2) -> {
                    long diff = o1.getTimestamp() - o2.getTimestamp();
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                Set<Integer> testItemsThisUser = new TreeSet<>();

                thisUserRatingsWithTimestamp.subList(firstTestItem, thisUserRatingsWithTimestamp.size()).stream().forEach((testRating) -> {
                    testItemsThisUser.add(testRating.getIdItem());
                });

                testSet.put(idUser, testItemsThisUser);
            } catch (UserNotFound ex) {
                Logger.getLogger(HoldOutGroupMemberTemporalRatings.class.getName()).log(Level.SEVERE, null, ex);
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

                Global.showInfoMessage("Dataset de training.\n");
                String printTrainingSet = DatasetPrinter.printCompactRatingTable(
                        ret[0].train,
                        allUsers,
                        datasetLoader.getRatingsDataset().allRatedItems());

                Global.showInfoMessage(printTrainingSet);

                Global.showInfoMessage("Dataset de test.\n");
                String printTestSet = DatasetPrinter.printCompactRatingTable(
                        ret[0].test,
                        allUsers,
                        datasetLoader.getRatingsDataset().allRatedItems());
                Global.showInfoMessage(printTestSet);
            }
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        }

        return ret;
    }

    private void checkParameterNotNull(DatasetLoader<? extends Rating> datasetLoader, Iterable<GroupOfUsers> groupsOfUsers) throws IllegalArgumentException {
        if (datasetLoader == null) {
            throw new IllegalArgumentException("The datasetLoader is null.");
        }

        if (groupsOfUsers == null) {
            throw new IllegalArgumentException("The parameter 'groupOfUsers' is null.");
        }
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

    private void checkRatingsWithTimestamp(RatingsDataset<? extends Rating> ratingsDataset) {
        for (Rating rating : ratingsDataset) {
            if (!(rating instanceof RatingWithTimestamp)) {
                throw new IllegalArgumentException("The ratings must be timestamped to use '" + getAlias() + "' validation!");
            }
        }
    }
}
