package delfos.rs.trustbased.belieffunctions;

import delfos.constants.DelfosTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @version 21-abr-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Type1BeliefTest extends DelfosTest {

    public Type1BeliefTest() {
    }

    /**
     * Test of beliefFromCorrelation method, of class Type1Belief.
     */
    @Test
    public void testBeliefZeroPointFive() {
        double correlation = 0.5;
        Type1Belief instance = new Type1Belief();
        double expResult = 0.85355;
        double result = instance.beliefFromCorrelation(correlation);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of beliefFromCorrelation method, of class Type1Belief.
     */
    @Test
    public void testBeliefZeroPointEight() {
        double correlation = 0.8;
        Type1Belief instance = new Type1Belief();
        double expResult = 0.97552825;
        double result = instance.beliefFromCorrelation(correlation);
        assertEquals(expResult, result, 0.00001);
    }

}
