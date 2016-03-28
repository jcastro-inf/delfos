package delfos.group.grs.cww;

import delfos.rs.trustbased.WeightedGraph;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class WeightedGraphMOCK extends WeightedGraph<Integer> {

    private static final Map<Integer, Map<Integer, Number>> CONNECTIONS;

    static {

        CONNECTIONS = new TreeMap<>();
        addNode(1, 2, 1.0);
        addNode(1, 4, 0.3);
        addNode(2, 3, 1.0);
        addNode(2, 4, 0.1);
        addNode(3, 4, 0.5);
        addNode(4, 5, 0.8);
    }

    public WeightedGraphMOCK() {
        super(CONNECTIONS);
    }

    private static void addNode(int node1, int node2, double weight) {

        if (!CONNECTIONS.containsKey(node1)) {
            CONNECTIONS.put(node1, new TreeMap<>());
        }
        if (!CONNECTIONS.containsKey(node2)) {
            CONNECTIONS.put(node2, new TreeMap<>());
        }

        CONNECTIONS.get(node1).put(node2, weight);
        CONNECTIONS.get(node2).put(node1, weight);

    }

}
