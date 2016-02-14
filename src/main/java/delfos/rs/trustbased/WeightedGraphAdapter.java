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

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.util.DatasetPrinter;

/**
 * Grafo ponderado.
 *
 * @param <Node> Tipo de los nodos del grafo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-May-2013
 */
public class WeightedGraphAdapter<Node> implements Serializable, WeightedGraph<Node> {

    private static final long serialVersionUID = 115L;
    protected final Map<Node, Map<Node, Number>> connections;
    protected final Set<Node> allNodes;

    public WeightedGraphAdapter() {

        this.allNodes = new TreeSet<Node>();
        this.connections = new TreeMap<Node, Map<Node, Number>>();
    }

    /**
     * Crea la red de confianza con los valores indicados.
     *
     * @param connections Valores de las conexiones entre los elementos.
     *
     * @throws IllegalArgumentException Si la estructura de valores de confianza
     * es nula.
     */
    public WeightedGraphAdapter(Map<Node, Map<Node, Number>> connections) {

        this();
        if (connections == null) {
            throw new IllegalArgumentException("The trust values structure cannot be null");
        }

        for (Node key1 : connections.keySet()) {
            this.connections.put(key1, new TreeMap<Node, Number>());
            allNodes.add(key1);

            for (Node key2 : connections.get(key1).keySet()) {
                Number value = connections.get(key1).get(key2);
                this.connections.get(key1).put(key2, value);
                this.allNodes.add(key2);
            }
        }
    }

    /**
     * Devuelve la intensidad de la conexión directa entre dos nodos.
     *
     * @param id1
     * @param id2
     * @return
     */
    @Override
    public Number connection(Node id1, Node id2) {
        if (connections.containsKey(id1)) {
            if (connections.get(id1).containsKey(id2)) {
                return connections.get(id1).get(id2);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Devuelve la intensidad de la conexión compuesta entre dos nodos.
     *
     * @param id1
     * @param id2
     * @param k Longitud del camino.
     * @return
     */
    @Override
    public double composition(Node id1, Node id2, int k) {
        if (k < 0) {
            throw new IllegalArgumentException("El número de composiciones no puede ser negativo.");
        }

        if (k > maxK()) {
            throw new IllegalArgumentException("El número de composiciones no puede ser mayor que " + maxK() + ", el valor fué " + k);
        }

        if (k == 0) {
            Number connection = connection(id1, id2);
            return connection.doubleValue();
        } else {
            double maxValue = 0;
            for (Node nodoIntermedio : allNodes) {
                if (nodoIntermedio == id1 || nodoIntermedio == id2) {
                    continue;
                }
                double v1 = composition(id1, nodoIntermedio, k - 1);
                double v2 = composition(nodoIntermedio, id2, k - 1);

                double value = compositionOfConnections(v1, v2);
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            return maxValue;
        }
    }

    /**
     * Longitud máxima de un camino sin repetir nodos.
     *
     * @return
     */
    @Override
    public int maxK() {
        return connections.size() - 1;
    }

    /**
     * Devuelve todos los nodos del grafo.
     *
     * @return
     */
    @Override
    public Collection<Node> allNodes() {
        return new ArrayList<Node>(allNodes);
    }

    private double compositionOfConnections(double conn1, double conn2) {
        return conn1 * conn2;

    }

    @Override
    public double geodesicDistance(Node n1, Node n2) {
        return composition(n1, n2, maxK());
    }

    public double distance(Node n1, Node n2) {
        PathBetweenNodes<Node> shortestPath = shortestPath(n1, n2);

        if (shortestPath == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return shortestPath.getLength();
        }
    }

    public PathBetweenNodes shortestPath(Node n1, Node n2) {

        if (n1 == n2) {
            ArrayList<Node> oneNodePath = new ArrayList<Node>();
            oneNodePath.add(n1);
            return new PathBetweenNodes(this, oneNodePath);
        }

        PriorityQueue<PathBetweenNodes> priorityList = new ObjectHeapPriorityQueue<PathBetweenNodes>();

        {
            Set<Node> allButFirst = new TreeSet<Node>(allNodes);
            allButFirst.remove(n1);
            allButFirst = Collections.unmodifiableSet(allButFirst);

            for (Node intermediateNode : allButFirst) {

                List<Node> path = new ArrayList<Node>();
                path.add(n1);
                path.add(intermediateNode);
                PathBetweenNodes pathBetweenNodes = new PathBetweenNodes(this, path);
                if (!Double.isInfinite(pathBetweenNodes.getLength())) {
                    priorityList.enqueue(pathBetweenNodes);
                }
            }
        }

        while (true) {
            PathBetweenNodes firsPath = priorityList.dequeue();
            if (firsPath.isPathBetween(n1, n2)) {
                return firsPath;
            }

            if (Double.isInfinite(firsPath.getLength())) {
                return null;
            }

            if (firsPath.numJumps() > allNodes.size()) {
                return null;
            }
            for (Node intermediateNode : allNodes) {
                if (!firsPath.getNodes().contains(intermediateNode)) {

                    List<Node> newPath = new ArrayList<Node>(firsPath.getNodes());
                    newPath.add(intermediateNode);

                    PathBetweenNodes pathBetweenNodes = new PathBetweenNodes(this, newPath);
                    if (!Double.isInfinite(pathBetweenNodes.getLength())) {
                        priorityList.enqueue(pathBetweenNodes);
                    }
                }
            }

            if (priorityList.isEmpty()) {
                break;
            }
        }

        return null;
    }

    /**
     * Devuelve la centralidad de un nodo en el grafo, utilizando k conexiones.
     *
     * @param node Nodo para el que se calcula la centralidad.
     * @param k Longitud de los caminos que se considera.
     * @return Valor de centralidad del nodo.
     */
    @Override
    public double centrality(Node node, int k) {

        double centralityValue = 0;
        for (Node node2 : allNodes) {
            if (node.equals(node2)) {
                continue;
            }

            centralityValue += composition(node, node2, k);
        }

        return centralityValue;
    }

    @Override
    public String toString() {
        String printWeightedGraph = DatasetPrinter.printWeightedGraph(this);
        return printWeightedGraph;
    }

}
