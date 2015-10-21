package delfos.dataset.computergenerateddatasets;

import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import delfos.dataset.generated.random.RandomRatingsDataset;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import delfos.common.datastructures.histograms.HistogramCategories;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.util.DatasetPrinter;
import delfos.dataset.util.RatingsDatasetDiff;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.rating.domain.IntegerDomain;
import delfos.dataset.basic.rating.domain.IntegerDomainWithProbabilities;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 5-marzo-2015
 */
public class RandomRatingsDatasetTest {

    public RandomRatingsDatasetTest() {
    }

    @Test
    public void testCreateDefault() {
        System.out.println("createDefault");
        RandomRatingsDataset result = RandomRatingsDatasetFactory.createDefault();

        String datasetPrinted = DatasetPrinter.printCompactRatingTable(result);

        System.out.println(datasetPrinted);

    }

    @Test
    public void testCreateDefault_withSeed() {
        System.out.println("testCreateDefault_withSeed");
        long seed = 1L;
        RandomRatingsDataset expResult = RandomRatingsDatasetFactory.createDefault(seed);
        RandomRatingsDataset result = RandomRatingsDatasetFactory.createDefault(seed);

        String expectedPrinted = DatasetPrinter.printCompactRatingTable(expResult);
        System.out.println(expectedPrinted);

        String resultPrinted = DatasetPrinter.printCompactRatingTable(result);
        System.out.println(resultPrinted);

        Assert.assertTrue(RatingsDatasetDiff.equalsRatingsValues(expResult, result));
    }

    @Test
    public void testCreateRatingsDatasetWithNumUserRatingsAndSeed() {
        System.out.println("testCreateRatingsDatasetWithNumUserRatingsAndSeed");
        Set<Integer> users = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Set<Integer> items = new TreeSet<>(Arrays.asList(901, 902, 903, 904, 905, 906, 907, 908, 909, 910));
        int numRatingsPerUser = 7;

        long seed = 1L;
        final IntegerDomain ratingDomain = new IntegerDomain(1l, 5l);

        RandomRatingsDataset expResult = RandomRatingsDatasetFactory.createRatingsDatasetWithNumUserRatings(users, items, numRatingsPerUser, ratingDomain, seed);
        RandomRatingsDataset result = RandomRatingsDatasetFactory.createRatingsDatasetWithNumUserRatings(users, items, numRatingsPerUser, ratingDomain, seed);

        String expectedPrinted = DatasetPrinter.printCompactRatingTable(expResult);
        System.out.println(expectedPrinted);

        String resultPrinted = DatasetPrinter.printCompactRatingTable(result);
        System.out.println(resultPrinted);

        Assert.assertTrue(RatingsDatasetDiff.equalsRatingsValues(expResult, result));
    }

    @Test
    public void testDatasetWithAGivenRatingDistribution() {
        Set<Integer> users = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Set<Integer> items = new TreeSet<>(Arrays.asList(901, 902, 903, 904, 905, 906, 907, 908, 909, 910));
        double loadFactor = 0.5;
        Domain ratingDomain = new IntegerDomainWithProbabilities(Arrays.asList(
                new IntegerDomainWithProbabilities.ValueWithProbability(1, 06110),
                new IntegerDomainWithProbabilities.ValueWithProbability(2, 11370),
                new IntegerDomainWithProbabilities.ValueWithProbability(3, 24145),
                new IntegerDomainWithProbabilities.ValueWithProbability(4, 34174),
                new IntegerDomainWithProbabilities.ValueWithProbability(5, 21201)
        ));
        long seed = 1L;

        RandomRatingsDataset result = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(users, items, loadFactor, ratingDomain, seed);

        String resultPrinted = DatasetPrinter.printCompactRatingTable(result);
        System.out.println(resultPrinted);

        HistogramCategories histogram = new HistogramCategories<>();

        for (Rating rating : result) {
            histogram.addValue(rating.getRatingValue().intValue());
        }
    }
}
