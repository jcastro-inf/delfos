package delfos.dataset.computergenerateddatasets;

import delfos.common.Global;
import delfos.common.datastructures.histograms.HistogramCategories;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.rating.domain.IntegerDomain;
import delfos.dataset.basic.rating.domain.IntegerDomainWithProbabilities;
import delfos.dataset.generated.random.RandomRatingsDataset;
import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import delfos.dataset.util.DatasetPrinter;
import delfos.dataset.util.RatingsDatasetDiff;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 5-marzo-2015
 */
public class RandomRatingsDatasetTest {

    public RandomRatingsDatasetTest() {
    }

    @Test
    public void testCreateDefault() {
        RandomRatingsDataset result = RandomRatingsDatasetFactory.createDefault();

        String datasetPrinted = DatasetPrinter.printCompactRatingTable(result);

        Global.showln(datasetPrinted);

    }

    @Test
    public void testCreateDefault_withSeed() {
        long seed = 1L;
        RandomRatingsDataset expResult = RandomRatingsDatasetFactory.createDefault(seed);
        RandomRatingsDataset result = RandomRatingsDatasetFactory.createDefault(seed);

        String expectedPrinted = DatasetPrinter.printCompactRatingTable(expResult);
        Global.showln(expectedPrinted);

        String resultPrinted = DatasetPrinter.printCompactRatingTable(result);
        Global.showln(resultPrinted);

        Assert.assertTrue(RatingsDatasetDiff.equalsRatingsValues(expResult, result));
    }

    @Test
    public void testCreateRatingsDatasetWithNumUserRatingsAndSeed() {
        Set<Long> users = LongStream.rangeClosed(1,9).boxed().collect(Collectors.toSet());
        Set<Long> items = LongStream.rangeClosed(901,910).boxed().collect(Collectors.toSet());

        int numRatingsPerUser = 7;

        long seed = 1L;
        final IntegerDomain ratingDomain = new IntegerDomain(1l, 5l);

        RandomRatingsDataset expResult = RandomRatingsDatasetFactory.createRatingsDatasetWithNumUserRatings(users, items, numRatingsPerUser, ratingDomain, seed);
        RandomRatingsDataset result = RandomRatingsDatasetFactory.createRatingsDatasetWithNumUserRatings(users, items, numRatingsPerUser, ratingDomain, seed);

        String expectedPrinted = DatasetPrinter.printCompactRatingTable(expResult);
        Global.showln(expectedPrinted);

        String resultPrinted = DatasetPrinter.printCompactRatingTable(result);
        Global.showln(resultPrinted);

        Assert.assertTrue(RatingsDatasetDiff.equalsRatingsValues(expResult, result));
    }

    @Test
    public void testDatasetWithAGivenRatingDistribution() {
        Set<Long> users = LongStream.rangeClosed(1,9).boxed().collect(Collectors.toSet());
        Set<Long> items = LongStream.rangeClosed(901,910).boxed().collect(Collectors.toSet());

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
        Global.showln(resultPrinted);

        HistogramCategories histogram = new HistogramCategories<>();

        for (Rating rating : result) {
            histogram.addValue(rating.getRatingValue().intValue());
        }
    }
}
