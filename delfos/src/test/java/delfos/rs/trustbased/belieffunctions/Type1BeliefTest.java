package delfos.rs.trustbased.belieffunctions;

import delfos.rs.trustbased.belieffunctions.Type1Belief;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import delfos.constants.DelfosTest;

/**
 *
 * @version 21-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type1BeliefTest extends DelfosTest {

    public Type1BeliefTest() {
    }

    /**
     * Test of beliefFromCorrelation method, of class Type1Belief.
     */
    @Test
    public void testBeliefZeroPointFive() {
        System.out.println("testBeliefZeroPointFive");
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
        System.out.println("testBeliefZeroPointEight");
        double correlation = 0.8;
        Type1Belief instance = new Type1Belief();
        double expResult = 0.97552825;
        double result = instance.beliefFromCorrelation(correlation);
        assertEquals(expResult, result, 0.00001);
    }

}
