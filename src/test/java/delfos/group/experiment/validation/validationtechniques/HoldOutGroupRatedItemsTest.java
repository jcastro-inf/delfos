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
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @version 24-jul-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class HoldOutGroupRatedItemsTest {

    public HoldOutGroupRatedItemsTest() {
    }

    /**
     * Test of getNumberOfSplits method, of class HoldOut_Items.
     */
    @Test
    public void testWithDatasetComplete5U10I() {
        DatasetLoader datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");

        List<GroupOfUsers> groupsOfUsers = new LinkedList<>();
        groupsOfUsers.add(new GroupOfUsers(1, 2, 3));

        HoldOutGroupRatedItems instance = new HoldOutGroupRatedItems();
        instance.setSeedValue(123456789);

        PairOfTrainTestRatingsDataset[] pairOfTrainTestRatingsDatasets = instance.shuffle(datasetLoader, groupsOfUsers);

        String trainDatasetString = DatasetPrinter.printCompactRatingTable(pairOfTrainTestRatingsDatasets[0].train);
        Global.showln("============ TRAINING =================");
        Global.showln(trainDatasetString);
        Global.showln("=======================================");

        String testDatasetString = DatasetPrinter.printCompactRatingTable(pairOfTrainTestRatingsDatasets[0].test);
        Global.showln("============ TEST =================");
        Global.showln(testDatasetString);
        Global.showln("=======================================");

        List<Rating> expectedRatings = new LinkedList<>();
        expectedRatings.add(new Rating(1, 6, 1));
        expectedRatings.add(new Rating(2, 6, 2));
        expectedRatings.add(new Rating(3, 6, 3));

        RatingsDataset<Rating> expectedTestSet = new BothIndexRatingsDataset<>(expectedRatings);
        assertEquals(expectedTestSet, pairOfTrainTestRatingsDatasets[0].test);
    }
}
