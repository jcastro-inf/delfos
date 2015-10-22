package delfos.dataset.util;

import delfos.common.Global;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class RatingsDatasetDiffTest {

    public RatingsDatasetDiffTest() {
    }

    /**
     * Test of datasetDiff method, of class RatingsDatasetDiff.
     */
    @Test
    public void testDatasetDiff() {
        RatingsDataset<? extends Rating> oldRatingsDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(10, 20, 0.5, new DecimalDomain(1, 5), 654321);
        RatingsDataset<? extends Rating> newRatingsDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(10, 20, 0.5, new DecimalDomain(1, 5), 123456);

        Global.showln("=============================================================");
        Global.showln("===================== Old dataset ===========================");
        Global.showln("=============================================================");
        Global.showln(DatasetPrinter.printCompactRatingTable(oldRatingsDataset));
        Global.showln("=============================================================");
        Global.showln("===================== New dataset ===========================");
        Global.showln("=============================================================");
        Global.showln(DatasetPrinter.printCompactRatingTable(newRatingsDataset));
        Global.showln("=============================================================");
        Global.showln("===================== Diff old to new =======================");
        Global.showln("=============================================================");
        Global.showln(RatingsDatasetDiff.printDiff(oldRatingsDataset, newRatingsDataset));
        Global.showln("=============================================================");
        Global.showln("===================== Diff new to old =======================");
        Global.showln("=============================================================");
        Global.showln(RatingsDatasetDiff.printDiff(newRatingsDataset, oldRatingsDataset));
        Global.showln("=============================================================");
        Global.showln("===================== Diff histogram old to new =============");
        Global.showln("=============================================================");
        Global.showln(RatingsDatasetDiff.printDiffHistogram(newRatingsDataset, oldRatingsDataset));
        Global.showln("=============================================================");
        Global.showln("===================== Diff histogram new to old =============");
        Global.showln("=============================================================");
        Global.showln(RatingsDatasetDiff.printDiffHistogram(oldRatingsDataset, newRatingsDataset));
    }

}
