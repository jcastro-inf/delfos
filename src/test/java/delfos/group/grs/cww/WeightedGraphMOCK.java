package delfos.group.grs.cww;

import delfos.rs.trustbased.WeightedGraph;
import java.util.TreeMap;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class WeightedGraphMOCK extends WeightedGraph<Integer> {

    public WeightedGraphMOCK() {
        super();

        addNode(1, 2, 1.0);
        addNode(1, 4, 0.3);
        addNode(2, 3, 1.0);
        addNode(2, 4, 0.1);
        addNode(3, 4, 0.5);
        addNode(4, 5, 0.8);
    }

    private void addNode(int node1, int node2, double weight) {

        if (!connections.containsKey(node1)) {
            connections.put(node1, new TreeMap<>());
            allNodes.add(node1);
        }
        if (!connections.containsKey(node2)) {
            connections.put(node2, new TreeMap<>());
            allNodes.add(node2);
        }

        connections.get(node1).put(node2, weight);
        connections.get(node2).put(node1, weight);

    }

}
