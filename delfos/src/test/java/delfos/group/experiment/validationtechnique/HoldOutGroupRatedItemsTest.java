package delfos.group.experiment.validationtechnique;

import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.dataset.util.DatasetPrinter;

/**
 *
 * @version 24-jul-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class HoldOutGroupRatedItemsTest {

    public HoldOutGroupRatedItemsTest() {
    }

    /**
     * Test of getNumberOfSplits method, of class HoldOut_Items.
     */
    @Test
    public void testWithDatasetComplete5U10I() {
        System.out.println("getNumberOfSplits");
        DatasetLoader datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");

        List<GroupOfUsers> groupsOfUsers = new LinkedList<>();
        groupsOfUsers.add(new GroupOfUsers(1, 2, 3));

        HoldOutGroupRatedItems instance = new HoldOutGroupRatedItems();
        instance.setSeedValue(123456789);

        PairOfTrainTestRatingsDataset[] pairOfTrainTestRatingsDatasets = instance.shuffle(datasetLoader, groupsOfUsers);

        String trainDatasetString = DatasetPrinter.printCompactRatingTable(pairOfTrainTestRatingsDatasets[0].train);
        System.out.println("============ TRAINING =================");
        System.out.println(trainDatasetString);
        System.out.println("=======================================");

        String testDatasetString = DatasetPrinter.printCompactRatingTable(pairOfTrainTestRatingsDatasets[0].test);
        System.out.println("============ TEST =================");
        System.out.println(testDatasetString);
        System.out.println("=======================================");

        List<Rating> expectedRatings = new LinkedList<>();
        expectedRatings.add(new Rating(1, 6, 1));
        expectedRatings.add(new Rating(2, 6, 2));
        expectedRatings.add(new Rating(3, 6, 3));

        RatingsDataset<Rating> expectedTestSet = new BothIndexRatingsDataset<>(expectedRatings);
        assertEquals(expectedTestSet, pairOfTrainTestRatingsDatasets[0].test);
    }
}
