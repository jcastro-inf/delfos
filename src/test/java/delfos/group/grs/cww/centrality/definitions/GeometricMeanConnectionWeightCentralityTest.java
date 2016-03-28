package delfos.group.grs.cww.centrality.definitions;

import delfos.group.grs.cww.WeightedGraphMOCK;
import delfos.rs.trustbased.WeightedGraph;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GeometricMeanConnectionWeightCentralityTest {

    public GeometricMeanConnectionWeightCentralityTest() {
    }

    @Test
    public void testCentralityOfUser() {

        WeightedGraph<Integer> weightedGraph = new WeightedGraphMOCK();

        GeometricMeanConnectionWeightCentrality instance = new GeometricMeanConnectionWeightCentrality();

        assertEquals(0.000, instance.centrality(weightedGraph, 1), 0.001);
        assertEquals(0.000, instance.centrality(weightedGraph, 2), 0.001);
        assertEquals(0.000, instance.centrality(weightedGraph, 3), 0.001);
        assertEquals(0.012, instance.centrality(weightedGraph, 4), 0.001);
        assertEquals(0.000, instance.centrality(weightedGraph, 5), 0.001);
    }
}
