package delfos.rs.trustbased.belieffunctions;

import delfos.rs.trustbased.belieffunctions.Type2Belief;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import delfos.constants.DelfosTest;

/**
 *
 * @version 21-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type2BeliefTest extends DelfosTest {

    public Type2BeliefTest() {
    }

    /**
     * Test of beliefFromCorrelation method, of class Type1Belief.
     */
    @Test
    public void testBeliefZeroPointFive() {
        System.out.println("testBeliefZeroPointFive");
        double correlation = 0.5;
        Type2Belief instance = new Type2Belief();
        double expResult = 0.66666666666666;
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
        Type2Belief instance = new Type2Belief();
        double expResult = 0.795167;
        double result = instance.beliefFromCorrelation(correlation);
        assertEquals(expResult, result, 0.00001);
    }

}
