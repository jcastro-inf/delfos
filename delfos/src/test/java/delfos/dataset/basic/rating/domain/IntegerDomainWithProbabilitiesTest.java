package delfos.dataset.basic.rating.domain;

import delfos.dataset.basic.rating.domain.IntegerDomainWithProbabilities;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class IntegerDomainWithProbabilitiesTest {

    private final static double DELTA = Double.MIN_VALUE;

    public IntegerDomainWithProbabilitiesTest() {
    }

    @Test
    public void noUniqueValuesMustFail() {
        System.out.println("noUniqueValuesMustFail");

        try {
            IntegerDomainWithProbabilities instance = new IntegerDomainWithProbabilities(
                    Arrays.asList(
                            new IntegerDomainWithProbabilities.ValueWithProbability(1, 0.1),
                            new IntegerDomainWithProbabilities.ValueWithProbability(2, 0.1),
                            new IntegerDomainWithProbabilities.ValueWithProbability(3, 0.1),
                            new IntegerDomainWithProbabilities.ValueWithProbability(3, 0.1),
                            new IntegerDomainWithProbabilities.ValueWithProbability(4, 0.1)
                    )
            );
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(IntegerDomainWithProbabilities.NOT_UNIQUES, ex.getMessage());
        }
    }

    @Test
    public void noCorrelativeValuesMustFail() {
        System.out.println("noCorrelativeValuesMustFail");

        try {
            IntegerDomainWithProbabilities instance = new IntegerDomainWithProbabilities(
                    Arrays.asList(
                            new IntegerDomainWithProbabilities.ValueWithProbability(1, 0.1),
                            new IntegerDomainWithProbabilities.ValueWithProbability(2, 0.1),
                            new IntegerDomainWithProbabilities.ValueWithProbability(4, 0.1),
                            new IntegerDomainWithProbabilities.ValueWithProbability(5, 0.1),
                            new IntegerDomainWithProbabilities.ValueWithProbability(6, 0.1)
                    )
            );
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(IntegerDomainWithProbabilities.NOT_CORRELATIVES, ex.getMessage());
        }
    }

    @Test
    public void noMinimumEqualMaximumMustFail() {
        System.out.println("noMinimumEqualMaximumMustFail");

        try {
            IntegerDomainWithProbabilities instance = new IntegerDomainWithProbabilities(
                    Arrays.asList(
                            new IntegerDomainWithProbabilities.ValueWithProbability(1, 0.1)
                    )
            );
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(IntegerDomainWithProbabilities.MINIMUM_EQUALS_MAXIMUM, ex.getMessage());
        }
    }

    @Test
    public void testMovieLens100kDistribution() {
        System.out.println("testMovieLens100kDistribution");

        IntegerDomainWithProbabilities ml100k = new IntegerDomainWithProbabilities(
                Arrays.asList(
                        new IntegerDomainWithProbabilities.ValueWithProbability(1, 6110),
                        new IntegerDomainWithProbabilities.ValueWithProbability(2, 11370),
                        new IntegerDomainWithProbabilities.ValueWithProbability(3, 27145),
                        new IntegerDomainWithProbabilities.ValueWithProbability(4, 34174),
                        new IntegerDomainWithProbabilities.ValueWithProbability(5, 21201)
                )
        );

        Assert.assertEquals(1d, ml100k.getValueAssociatedToProbability(0).doubleValue(), DELTA);
        Assert.assertEquals(1l, ml100k.getValueAssociatedToProbability(0.061), DELTA);

        Assert.assertEquals(2l, ml100k.getValueAssociatedToProbability(0.0611), DELTA);
        Assert.assertEquals(2l, ml100k.getValueAssociatedToProbability(0.1747), DELTA);

        Assert.assertEquals(3l, ml100k.getValueAssociatedToProbability(0.1748), DELTA);
        Assert.assertEquals(3l, ml100k.getValueAssociatedToProbability(0.44625), DELTA);

        Assert.assertEquals(4l, ml100k.getValueAssociatedToProbability(0.44626), DELTA);
        Assert.assertEquals(4l, ml100k.getValueAssociatedToProbability(0.78798), DELTA);

        Assert.assertEquals(5l, ml100k.getValueAssociatedToProbability(0.78799), DELTA);
        Assert.assertEquals(5l, ml100k.getValueAssociatedToProbability(1.00000), DELTA);
    }
}
