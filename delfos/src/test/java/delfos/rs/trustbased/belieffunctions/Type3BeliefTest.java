package delfos.rs.trustbased.belieffunctions;

import delfos.rs.trustbased.belieffunctions.Type3Belief;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import delfos.constants.DelfosTest;

/**
 *
 * @version 21-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type3BeliefTest extends DelfosTest {

    public Type3BeliefTest() {
    }

    /**
     * Test of beliefFromCorrelation method, of class Type1Belief.
     */
    @Test
    public void testBeliefZeroPointFive_KequalsThree() {
        System.out.println("testBeliefZeroPointFive_Kequals3");
        double correlation = 0.5;
        Type3Belief instance = new Type3Belief(3);
        double expResult = 0.896850;
        double result = instance.beliefFromCorrelation(correlation);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of beliefFromCorrelation method, of class Type1Belief.
     */
    @Test
    public void testBeliefZeroPointFive_KequalsFive() {
        System.out.println("testBeliefZeroPointFive_Kequals3");
        double correlation = 0.5;
        Type3Belief instance = new Type3Belief(5);
        double expResult = 0.935275;
        double result = instance.beliefFromCorrelation(correlation);
        assertEquals(expResult, result, 0.00001);
    }

}
