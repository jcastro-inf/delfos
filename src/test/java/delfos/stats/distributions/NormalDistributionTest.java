package delfos.stats.distributions;

import delfos.stats.distributions.NormalDistribution;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class NormalDistributionTest {

    public NormalDistributionTest() {
    }

    /**
     * Test of z method, of class NormalDistribution.
     */
    @Test
    public void testZ_at_ConfidenceOfpoint95() {
        double expResult = 1.96;
        double result = NormalDistribution.z(.95);
        assertEquals(expResult, result, .0001);
    }
}
