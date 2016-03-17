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

import com.google.common.util.concurrent.AtomicDouble;
import delfos.dataset.util.DatasetPrinter;
import dnl.utils.text.table.TextTable;
import edu.princeton.cs.algs4.AdjMatrixEdgeWeightedDigraph;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.FloydWarshall;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.output.WriterOutputStream;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @param <Node>
 */
public class WeightedGraph<Node> implements Serializable {

    private static final long serialVersionUID = 115L;

    protected final AdjMatrixEdgeWeightedDigraph adjMatrixEdgeWeightedDigraph;
    protected final Map<Node, Integer> nodesIndex;
    protected final Map<Integer, Node> nodesByIndex;

    private FloydWarshall floydWarshall = null;

    public WeightedGraph() {
        this.nodesIndex = new TreeMap<>();
        this.nodesByIndex = new TreeMap<>();
        this.adjMatrixEdgeWeightedDigraph = new AdjMatrixEdgeWeightedDigraph(0);
    }

    /**
     * Crea la red de confianza con los valores indicados.
     *
     * @param connections Valores de las conexiones entre los elementos.
     *
     * @throws IllegalArgumentException Si la estructura de valores de confianza
     * es nula.
     */
    public WeightedGraph(Map<Node, Map<Node, Number>> connections) {

        if (connections == null) {
            throw new IllegalArgumentException("The trust values structure cannot be null");
        }

        nodesIndex = makeIndex(connections);
        nodesByIndex = makeNodesByIndex(nodesIndex);

        adjMatrixEdgeWeightedDigraph = makeWeightedDiGraph(connections);
    }

    /**
     * Crea la red de confianza con los valores indicados.
     *
     *
     * @param matrix Connections
     * @param ordering ordering of both columns and rows of the matrix
     * @throws IllegalArgumentException Si la estructura de valores de confianza
     * es nula.
     */
    public WeightedGraph(double[][] matrix, List<Node> ordering) {

        nodesIndex = makeIndex(ordering);
        nodesByIndex = makeNodesByIndex(nodesIndex);

        adjMatrixEdgeWeightedDigraph = makeWeightedDiGraph(ordering, matrix);
    }

    private AdjMatrixEdgeWeightedDigraph makeWeightedDiGraph(List<Node> ordering, double[][] distanceMatrix) {
        AdjMatrixEdgeWeightedDigraph adjMatrixEdgeWeightedDigraph = new AdjMatrixEdgeWeightedDigraph(nodesIndex.size());

        ordering.stream().forEach(node1 -> {
            int indexNode1 = nodesIndex.get(node1);
            ordering.stream().forEach(node2 -> {
                int indexNode2 = nodesIndex.get(node2);
                double distance = distanceMatrix[indexNode1][indexNode2];

                if (distance > 0) {
                    adjMatrixEdgeWeightedDigraph.addEdge(new DirectedEdge(indexNode1, indexNode2, distance));
                }
            });
        });

        return adjMatrixEdgeWeightedDigraph;
    }

    public double connectionWeight(Node node1, Node node2) {

        return 1 / connectionDistance(node1, node2);
    }

    private double connectionDistance(Node node1, Node node2) {
        return connection(node1, node2);
    }

    /**
     * Devuelve la intensidad de la conexión directa entre dos nodos.
     *
     * @param node1
     * @param node2
     * @return
     */
    private double connection(Node node1, Node node2) {
        int indexNode1 = nodesIndex.get(node1);
        int indexNode2 = nodesIndex.get(node2);

        AtomicDouble weight = new AtomicDouble(Double.POSITIVE_INFINITY);

        adjMatrixEdgeWeightedDigraph.adj(indexNode1).forEach(edge -> {
            if (edge.to() == indexNode2) {
                weight.set(edge.weight());
            }
        });

        if (weight.get() == -1) {
            return 0;
        } else {
            return weight.get();
        }
    }

    /**
     * Longitud máxima de un camino sin repetir nodos.
     *
     * @return
     */
    public int maxK() {
        return nodesIndex.size() - 1;
    }

    /**
     * Devuelve todos los nodos del grafo.
     *
     * @return
     */
    public Collection<Node> allNodes() {
        return new ArrayList<>(nodesIndex.keySet());
    }

    public double geodesicDistance(Node node1, Node node2) {
        return shortestPath(node1, node2).getLength();
    }

    public double distance(Node n1, Node n2) {
        PathBetweenNodes<Node> shortestPath = shortestPath(n1, n2);

        if (shortestPath == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return shortestPath.getLength();
        }
    }

    public PathBetweenNodes<Node> shortestPath(Node node1, Node node2) {

        int indexNode1 = nodesIndex.get(node1);
        int indexNode2 = nodesIndex.get(node2);

        synchronized (this) {
            if (floydWarshall == null) {
                floydWarshall = new FloydWarshall(adjMatrixEdgeWeightedDigraph);
            }
        }

        ArrayList<Node> pathNodesIncludingStartAndEnd = new ArrayList<>();

        pathNodesIncludingStartAndEnd.add(node1);
        if (floydWarshall.hasPath(indexNode1, indexNode2)) {
            List<DirectedEdge> path = new ArrayList<>();

            double dist = floydWarshall.dist(indexNode1, indexNode2);
            final Iterable<DirectedEdge> edgeIterator = floydWarshall.path(indexNode1, indexNode2);

            for (DirectedEdge edge : edgeIterator) {
                path.add(edge);
            }

            path.stream().sequential().map(edge -> edge.to()).forEach(indexNodeIntermediate -> {
                Node nodeIntermediate = nodesByIndex.get(indexNodeIntermediate);
                pathNodesIncludingStartAndEnd.add(nodeIntermediate);
            });

            return new PathBetweenNodes(this, pathNodesIncludingStartAndEnd);

        } else {
            return null;
        }

    }

    @Override
    public String toString() {
        String printWeightedGraph = DatasetPrinter.printWeightedGraph(this);
        return printWeightedGraph;
    }

    public Double[][] asMatrix() {

        final List<Node> nodesSorted = nodesSortingForMatrix();

        Double[][] matrix = new Double[nodesSorted.size()][nodesSorted.size()];

        for (int indexRow = 0; indexRow < nodesSorted.size(); indexRow++) {
            Node node = nodesSorted.get(indexRow);

            for (int indexColumn = 0; indexColumn < nodesSorted.size(); indexColumn++) {
                Node node2 = nodesSorted.get(indexColumn);
                double value = connectionWeight(node, node2);
                matrix[indexRow][indexColumn] = value;
            }
        }

        return matrix;
    }

    public double[][] asMatrixUnboxed() {

        final List<Node> nodesSorted = nodesSortingForMatrix();

        double[][] matrix = new double[nodesSorted.size()][nodesSorted.size()];

        for (int indexRow = 0; indexRow < nodesSorted.size(); indexRow++) {
            Node node = nodesSorted.get(indexRow);

            for (int indexColumn = 0; indexColumn < nodesSorted.size(); indexColumn++) {
                Node node2 = nodesSorted.get(indexColumn);
                double value = connectionWeight(node, node2);
                matrix[indexRow][indexColumn] = value;
            }
        }

        return matrix;
    }

    public List<Node> nodesSortingForMatrix() {
        List<Node> nodesSorted = allNodes().stream().sorted().collect(Collectors.toList());
        return Collections.unmodifiableList(nodesSorted);
    }

    public void printTable(PrintStream outputStream) {
        TextTable textTable = getTextTable();
        textTable.printTable(outputStream, 0);
    }

    public TextTable getTextTable() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("node\\node");
        final List<Node> sortedNodes = this.allNodes().stream().sorted().collect(Collectors.toList());
        Object[][] data = new Object[sortedNodes.size()][sortedNodes.size() + 1];
        columnNames.addAll(sortedNodes.stream().map(node -> node.toString()).collect(Collectors.toList()));
        DecimalFormat format = new DecimalFormat("0.0000");
        for (int node1index = 0; node1index < sortedNodes.size(); node1index++) {
            Node node1 = sortedNodes.get(node1index);
            int row = node1index;

            data[row][0] = node1.toString();

            for (int node2index = 0; node2index < sortedNodes.size(); node2index++) {
                Node node2 = sortedNodes.get(node2index);
                int column = node2index + 1;

                double connection = connectionDistance(node1, node2);
                data[row][column] = format.format(connection);
            }
        }
        TextTable textTable = new TextTable(columnNames.toArray(new String[0]), data);
        return textTable;
    }

    public TextTable getTextTable(Set<Node> nodes) {
        validateParameters(nodes);

        List<String> columnNames = new ArrayList<>();
        columnNames.add("node\\node");
        final List<Node> sortedNodes = this.allNodes().stream().sorted().filter(node -> nodes.contains(node)).collect(Collectors.toList());
        Object[][] data = new Object[sortedNodes.size()][sortedNodes.size() + 1];
        columnNames.addAll(sortedNodes.stream().map(node -> node.toString()).collect(Collectors.toList()));
        DecimalFormat format = new DecimalFormat("0.0000");
        for (int node1index = 0; node1index < sortedNodes.size(); node1index++) {
            Node node1 = sortedNodes.get(node1index);
            int row = node1index;

            data[row][0] = node1.toString();

            for (int node2index = 0; node2index < sortedNodes.size(); node2index++) {
                Node node2 = sortedNodes.get(node2index);
                int column = node2index + 1;

                double connection = connectionDistance(node1, node2);
                data[row][column] = format.format(connection);
            }
        }
        TextTable textTable = new TextTable(columnNames.toArray(new String[0]), data);
        return textTable;
    }

    void validateParameters(Set<Node> nodes) throws IllegalArgumentException {
        boolean allMatch = nodes.parallelStream().allMatch(node -> this.allNodes().contains(node));
        if (!allMatch) {
            throw new IllegalArgumentException("Specified nodes are not present in the weighted graph");
        }
    }

    public void printTable(WriterOutputStream outputStream) {
        printTable(new PrintStream(outputStream));
    }

    public String toStringTable() {
        TextTable textTable = getTextTable();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        textTable.printTable(recordingStream, 0);

        return baos.toString();
    }

    public String toStringTable(Set<Node> nodes) {
        TextTable textTable = getTextTable(nodes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        textTable.printTable(recordingStream, 0);

        return baos.toString();
    }

    public AdjMatrixEdgeWeightedDigraph getWeightedDiGraph() {
        return cloneAdjMatrixEdgeWeightedDigraph(adjMatrixEdgeWeightedDigraph);
    }

    public static final AdjMatrixEdgeWeightedDigraph cloneAdjMatrixEdgeWeightedDigraph(AdjMatrixEdgeWeightedDigraph adjMatrixEdgeWeightedDigraph) {
        AdjMatrixEdgeWeightedDigraph copy = new AdjMatrixEdgeWeightedDigraph(adjMatrixEdgeWeightedDigraph.V(), adjMatrixEdgeWeightedDigraph.E());

        IntStream.range(0, adjMatrixEdgeWeightedDigraph.V()).sequential().forEach(vertex -> {
            for (DirectedEdge directedEdge : adjMatrixEdgeWeightedDigraph.adj(vertex)) {
                copy.addEdge(directedEdge);
            }

        });

        return copy;
    }

    protected final Map<Node, Integer> makeIndex(Map<Node, Map<Node, Number>> connections) {
        List<Node> sortedUsers = connections.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        return makeIndex(sortedUsers);
    }

    protected final Map<Node, Integer> makeIndex(List<Node> nodes) {
        return IntStream.range(0, nodes.size()).boxed()
                .collect(Collectors.toMap(
                        indexUser -> nodes.get(indexUser),
                        indexUser -> indexUser)
                );
    }

    protected final AdjMatrixEdgeWeightedDigraph makeWeightedDiGraph(Map<Node, Map<Node, Number>> connections) {

        long numEdges = connections.values().parallelStream()
                .flatMap(thisUserConnections -> thisUserConnections.values().stream())
                .filter(connection -> !Double.isNaN(connection.doubleValue()))
                .filter(connection -> connection.doubleValue() > 0)
                .count();

        AdjMatrixEdgeWeightedDigraph weightedDigraph = new AdjMatrixEdgeWeightedDigraph(nodesIndex.size(), (int) numEdges);

        connections.entrySet().parallelStream().forEach(entry -> {

            Node node1 = entry.getKey();
            int indexNode1 = nodesIndex.get(node1);
            Map<Node, Number> thisUserConnections = entry.getValue();

            thisUserConnections.entrySet().forEach(entry2 -> {
                Node node2 = entry2.getKey();
                int indexNode2 = nodesIndex.get(node2);
                Number connection = entry2.getValue();
                weightedDigraph.addEdge(new DirectedEdge(indexNode1, indexNode2, connection.doubleValue()));
            });
        });

        return weightedDigraph;
    }

    protected final Map< Integer, Node> makeNodesByIndex(Map<Node, Integer> nodesIndex) {
        return nodesIndex.entrySet().parallelStream().collect(Collectors.toMap(
                entry -> entry.getValue(),
                entry -> entry.getKey()));
    }

    public double distanceJumpLimited(Node node1, Node node2, int maxjumps) {
        PathBetweenNodes<Node> shortestPath = this.shortestPath(node1, node2);
        if (shortestPath.numJumps() > maxjumps) {
            return Double.NaN;
        } else {
            return shortestPath.getLength();
        }
    }
}
