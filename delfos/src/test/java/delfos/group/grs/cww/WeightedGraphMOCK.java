package delfos.group.grs.cww;

import java.util.TreeMap;
import delfos.rs.trustbased.WeightedGraphAdapter;

/**
 *
 * @author Jorge Castro Gallardo.
 *
 */
public class WeightedGraphMOCK extends WeightedGraphAdapter<Integer> {

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
            connections.put(node1, new TreeMap<Integer, Number>());
            allNodes.add(node1);
        }
        if (!connections.containsKey(node2)) {
            connections.put(node2, new TreeMap<Integer, Number>());
            allNodes.add(node2);
        }

        connections.get(node1).put(node2, weight);
        connections.get(node2).put(node1, weight);

    }

}
