/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.rs.trustbased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 04-mar-2014
 * @param <Node>
 */
public class PathBetweenNodes<Node> implements Comparable<PathBetweenNodes> {

    private final List<Node> _nodes;
    private final double length;

    public double getLength() {
        return length;
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(_nodes);
    }

    public PathBetweenNodes(WeightedGraph<Node> graph, List<Node> nodes) {

        _nodes = Collections.unmodifiableList(new ArrayList<Node>(nodes));

        double _length = 0;
        for (int i = 1; i < nodes.size(); i++) {

            Node previousNode = nodes.get(i - 1);
            Node thisNode = nodes.get(i);

            if (previousNode == thisNode) {
                //Salto sobre el mismo nodo.
            } else {
                double weight = graph.connection(previousNode, thisNode).doubleValue();

                if (weight == 0) {
                    _length = Double.POSITIVE_INFINITY;
                } else {
                    _length += 1 / weight;
                }
            }
        }
        this.length = _length;
    }

    public int compareTo(PathBetweenNodes o) {
        if (this.length == o.length) {
            return 0;
        } else {
            if (this.length > o.length) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public Node getFirst() {
        return _nodes.get(0);
    }

    public Node getLast() {
        return _nodes.get(_nodes.size() - 1);
    }

    public boolean isPathBetween(Node n1, Node n2) {
        return getFirst().equals(n1) && getLast().equals(n2);
    }

    public int numJumps() {
        return _nodes.size();
    }

    @Override
    public String toString() {
        return _nodes.toString() + " --> " + length;
    }

}
