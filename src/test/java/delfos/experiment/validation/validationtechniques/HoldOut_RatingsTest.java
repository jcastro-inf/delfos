package delfos.experiment.validation.validationtechniques;

import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.util.DatasetPrinter;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import java.util.ArrayList;
import java.util.Collection;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Jorge
 */
public class HoldOut_RatingsTest {

    public HoldOut_RatingsTest() {
    }

    /**
     * Test of shuffle method, of class HoldOut_Ratings.
     */
    @Test
    public void testShuffle() {
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");
        HoldOut_Ratings instance = new HoldOut_Ratings();
        instance.setSeedValue(123456789);

        PairOfTrainTestRatingsDataset[] shuffle = instance.shuffle(datasetLoader);
        Global.showln(" ============================ TEST ============================");
        Global.showln(DatasetPrinter.printCompactRatingTable(shuffle[0].test));
        Global.showln(" ==============================================================");

        RatingsDataset<? extends Rating> expResultTest = shuffleExpectedTestSet();
        RatingsDataset<? extends Rating> resultTest = shuffle[0].test;

        if (!expResultTest.equals(resultTest)) {

            Global.showln(" ============================ TEST COMPUTED ===================");
            Global.showln(DatasetPrinter.printCompactRatingTable(resultTest));
            Global.showln(" ==============================================================");
            Global.showln(" ============================ TEST EXPECTED ===================");
            Global.showln(DatasetPrinter.printCompactRatingTable(expResultTest));
            Global.showln(" ==============================================================");

            fail("The test set is different to the expected value.");
        }

    }

    private RatingsDataset<? extends Rating> shuffleExpectedTestSet() {
        Collection<Rating> ratings = new ArrayList<>();

        ratings.add(new Rating(1, 1, 1.0));
        ratings.add(new Rating(1, 6, 1));

        ratings.add(new Rating(2, 4, 2));
        ratings.add(new Rating(2, 6, 2));

        ratings.add(new Rating(3, 1, 3));
        ratings.add(new Rating(3, 6, 3));

        ratings.add(new Rating(4, 4, 4));
        ratings.add(new Rating(4, 6, 4));

        ratings.add(new Rating(5, 8, 5));
        ratings.add(new Rating(5, 10, 5));

        return new BothIndexRatingsDataset<>(ratings);
    }

}
