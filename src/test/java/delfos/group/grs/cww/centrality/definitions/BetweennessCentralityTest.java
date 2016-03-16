package delfos.group.grs.cww.centrality.definitions;

import delfos.group.grs.cww.centrality.definitions.BetweennessCentrality;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import delfos.group.grs.cww.WeightedGraphMOCK;
import delfos.rs.trustbased.WeightedGraph;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class BetweennessCentralityTest {

    public BetweennessCentralityTest() {
    }

    @Test
    public void testCentralityOfUser() {

        WeightedGraph<Integer> weightedGraph = new WeightedGraphMOCK();

        BetweennessCentrality instance = new BetweennessCentrality();

        assertEquals(0.00000000000000, instance.centrality(weightedGraph, 1), 0.001);
        assertEquals(0.16666666666666, instance.centrality(weightedGraph, 2), 0.001);
        assertEquals(0.33333333333333, instance.centrality(weightedGraph, 3), 0.001);
        assertEquals(0.50000000000000, instance.centrality(weightedGraph, 4), 0.001);
        assertEquals(0.00000000000000, instance.centrality(weightedGraph, 5), 0.001);
    }

}
