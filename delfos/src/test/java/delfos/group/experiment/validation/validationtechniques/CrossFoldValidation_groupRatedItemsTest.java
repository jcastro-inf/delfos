package delfos.group.experiment.validation.validationtechniques;

import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CrossFoldValidation_groupRatedItemsTest {

    public CrossFoldValidation_groupRatedItemsTest() {
    }

    @Test
    public void testWithDatasetComplete5U10I() {
        DatasetLoader datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");

        List<GroupOfUsers> groupsOfUsers = new LinkedList<>();
        groupsOfUsers.add(new GroupOfUsers(1, 2, 3));

        CrossFoldValidation_groupRatedItems instance = new CrossFoldValidation_groupRatedItems();
        instance.setSeedValue(123456789);

        PairOfTrainTestRatingsDataset[] pairOfTrainTestRatingsDatasets = instance.shuffle(datasetLoader, groupsOfUsers);

        Collection<Integer> allUsers = datasetLoader.getUsersDataset().getAllID();
        Collection<Integer> allItems = datasetLoader.getContentDataset().getAllID();

        for (int split = 0; split < pairOfTrainTestRatingsDatasets.length; split++) {
            String trainDatasetString = DatasetPrinter.printCompactRatingTable(pairOfTrainTestRatingsDatasets[split].train, allUsers, allItems);
            Global.showln("============ TRAINING [" + split + "] =================");
            Global.showln(trainDatasetString);
            Global.showln("=======================================");

            String testDatasetString = DatasetPrinter.printCompactRatingTable(pairOfTrainTestRatingsDatasets[split].test, allUsers, allItems);
            Global.showln("============ TEST [" + split + "] =================");
            Global.showln(testDatasetString);
            Global.showln("=======================================");

        }

        RatingsDataset<Rating> expectedTestSet_partition0 = new BothIndexRatingsDataset<>(Arrays.asList(
                new Rating(1, 6, 1),
                new Rating(2, 6, 2),
                new Rating(3, 6, 3),
                new Rating(1, 10, 1),
                new Rating(2, 10, 2),
                new Rating(3, 10, 3)
        ));

        RatingsDataset<Rating> expectedTestSet_partition1 = new BothIndexRatingsDataset<>(Arrays.asList(
                new Rating(1, 1, 1),
                new Rating(2, 1, 2),
                new Rating(3, 1, 3),
                new Rating(1, 9, 1),
                new Rating(2, 9, 2),
                new Rating(3, 9, 3)
        ));

        RatingsDataset<Rating> expectedTestSet_partition2 = new BothIndexRatingsDataset<>(Arrays.asList(
                new Rating(1, 4, 1),
                new Rating(2, 4, 2),
                new Rating(3, 4, 3),
                new Rating(1, 5, 1),
                new Rating(2, 5, 2),
                new Rating(3, 5, 3)
        ));

        RatingsDataset<Rating> expectedTestSet_partition3 = new BothIndexRatingsDataset<>(Arrays.asList(
                new Rating(1, 7, 1),
                new Rating(2, 7, 2),
                new Rating(3, 7, 3),
                new Rating(1, 8, 1),
                new Rating(2, 8, 2),
                new Rating(3, 8, 3)
        ));

        RatingsDataset<Rating> expectedTestSet_partition4 = new BothIndexRatingsDataset<>(Arrays.asList(
                new Rating(1, 2, 1),
                new Rating(2, 2, 2),
                new Rating(3, 2, 3),
                new Rating(1, 3, 1),
                new Rating(2, 3, 2),
                new Rating(3, 3, 3)
        ));

        assertEquals("Expected test set does not matches!", expectedTestSet_partition0, pairOfTrainTestRatingsDatasets[0].test);
        assertEquals("Expected test set does not matches!", expectedTestSet_partition1, pairOfTrainTestRatingsDatasets[1].test);
        assertEquals("Expected test set does not matches!", expectedTestSet_partition2, pairOfTrainTestRatingsDatasets[2].test);
        assertEquals("Expected test set does not matches!", expectedTestSet_partition3, pairOfTrainTestRatingsDatasets[3].test);
        assertEquals("Expected test set does not matches!", expectedTestSet_partition4, pairOfTrainTestRatingsDatasets[4].test);
    }

    @Test
    public void testWithDatasetComplete5U10I_twoGroups() {
        DatasetLoader datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");

        List<GroupOfUsers> groupsOfUsers = Arrays.asList(
                new GroupOfUsers(4, 5),
                new GroupOfUsers(1, 2, 3)
        );

        CrossFoldValidation_groupRatedItems instance = new CrossFoldValidation_groupRatedItems();
        instance.setSeedValue(123456789);

        PairOfTrainTestRatingsDataset[] pairOfTrainTestRatingsDatasets = instance.shuffle(datasetLoader, groupsOfUsers);

        Collection<Integer> allUsers = datasetLoader.getUsersDataset().getAllID();
        Collection<Integer> allItems = datasetLoader.getContentDataset().getAllID();

        for (int split = 0; split < pairOfTrainTestRatingsDatasets.length; split++) {
            String trainDatasetString = DatasetPrinter.printCompactRatingTable(pairOfTrainTestRatingsDatasets[split].train, allUsers, allItems);
            Global.showln("============ TRAINING [" + split + "] =================");
            Global.showln(trainDatasetString);
            Global.showln("=======================================");

            String testDatasetString = DatasetPrinter.printCompactRatingTable(pairOfTrainTestRatingsDatasets[split].test, allUsers, allItems);
            Global.showln("============ TEST [" + split + "] =================");
            Global.showln(testDatasetString);
            Global.showln("=======================================");

        }

        System.out.println("");
    }
}
