package delfos.group.experiment.validation.validationtechniques;

import delfos.group.experiment.validation.validationtechniques.HoldOutGroupMemberTemporalRatings;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.util.DatasetPrinter;
import delfos.dataset.util.RatingsDatasetDiff;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 *
 * @author jcastro
 */
public class HoldOutGroupMemberTemporalRatingsTest {

    public HoldOutGroupMemberTemporalRatingsTest() {
    }

    @Test
    public void testShuffle() throws UserNotFound {
        System.out.println("shuffle");
        RandomDatasetLoader datasetLoader = new RandomDatasetLoader(40, 20, 0.5);
        datasetLoader.setSeedValue(123456);
        Iterable<GroupOfUsers> groupsOfUsers = new FixedGroupSize_OnlyNGroups(10, 3).shuffle(datasetLoader);
        HoldOutGroupMemberTemporalRatings instance = new HoldOutGroupMemberTemporalRatings();

        PairOfTrainTestRatingsDataset[] result = instance.shuffle(datasetLoader, groupsOfUsers);
        RatingsDataset<? extends Rating> train = result[0].train;
        RatingsDataset<? extends Rating> test = result[0].test;

        System.out.println("================== TRAIN ================================");
        System.out.println(DatasetPrinter.printCompactRatingTable(train));
        System.out.println("================== TEST  ================================");
        System.out.println(DatasetPrinter.printCompactRatingTable(test));

        System.out.println("================== Test ratings over original ============");
        System.out.println(RatingsDatasetDiff.printDiff(datasetLoader.getRatingsDataset(), result[0].train));

        for (int idUser : test.allUsers()) {
            Collection<? extends Rating> trainingRatings = train.getUserRatingsRated(idUser).values();
            Collection<? extends Rating> testRatings = test.getUserRatingsRated(idUser).values();

            for (Rating trainingRating : trainingRatings) {
                for (Rating testRating : testRatings) {
                    Assert.assertTrue("All training tests must be older than the test set ones", true);
                }
            }
        }
    }
}
