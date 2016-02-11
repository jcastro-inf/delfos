/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
