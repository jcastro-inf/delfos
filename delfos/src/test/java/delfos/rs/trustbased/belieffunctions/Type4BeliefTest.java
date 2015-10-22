package delfos.rs.trustbased.belieffunctions;

import delfos.constants.DelfosTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @version 21-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type4BeliefTest extends DelfosTest {

    public Type4BeliefTest() {
    }

    /**
     * Test of beliefFromCorrelation method, of class Type1Belief.
     */
    @Test
    public void testBeliefZeroPointFive_KequalsThree() {
        double correlation = 0.5;
        Type4Belief instance = new Type4Belief(3);
        double expResult = 0.5625;
        double result = instance.beliefFromCorrelation(correlation);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of beliefFromCorrelation method, of class Type1Belief.
     */
    @Test
    public void testBeliefZeroPointFive_KequalsFive() {
        double correlation = 0.5;
        Type4Belief instance = new Type4Belief(5);
        double expResult = 0.515625;
        double result = instance.beliefFromCorrelation(correlation);
        assertEquals(expResult, result, 0.00001);
    }

}
