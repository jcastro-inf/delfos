package delfos.dataset.util;

import delfos.dataset.util.RatingsDatasetDiff;
import delfos.dataset.util.DatasetPrinter;
import org.junit.Test;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import delfos.dataset.basic.rating.domain.DecimalDomain;

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
        System.out.println("datasetDiff");
        RatingsDataset<? extends Rating> oldRatingsDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(10, 20, 0.5, new DecimalDomain(1, 5), 654321);
        RatingsDataset<? extends Rating> newRatingsDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(10, 20, 0.5, new DecimalDomain(1, 5), 123456);

        System.out.println("=============================================================");
        System.out.println("===================== Old dataset ===========================");
        System.out.println("=============================================================");
        System.out.println(DatasetPrinter.printCompactRatingTable(oldRatingsDataset));
        System.out.println("=============================================================");
        System.out.println("===================== New dataset ===========================");
        System.out.println("=============================================================");
        System.out.println(DatasetPrinter.printCompactRatingTable(newRatingsDataset));
        System.out.println("=============================================================");
        System.out.println("===================== Diff old to new =======================");
        System.out.println("=============================================================");
        System.out.println(RatingsDatasetDiff.printDiff(oldRatingsDataset, newRatingsDataset));
        System.out.println("=============================================================");
        System.out.println("===================== Diff new to old =======================");
        System.out.println("=============================================================");
        System.out.println(RatingsDatasetDiff.printDiff(newRatingsDataset, oldRatingsDataset));
        System.out.println("=============================================================");
        System.out.println("===================== Diff histogram old to new =============");
        System.out.println("=============================================================");
        System.out.println(RatingsDatasetDiff.printDiffHistogram(newRatingsDataset, oldRatingsDataset));
        System.out.println("=============================================================");
        System.out.println("===================== Diff histogram new to old =============");
        System.out.println("=============================================================");
        System.out.println(RatingsDatasetDiff.printDiffHistogram(oldRatingsDataset, newRatingsDataset));
    }

}
