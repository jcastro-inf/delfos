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

import delfos.common.StringsOrderings;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.output.WriterOutputStream;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @param <Node>
 */
public class WeightedGraph<Node> implements Serializable, Comparable<WeightedGraph<Node>> {

    private static final long serialVersionUID = 115L;

    protected final AdjMatrixEdgeWeightedDigraph adjMatrixEdgeWeightedDigraph;
    protected final Map<Node, Integer> nodesIndex;
    protected final Map<Integer, Node> nodesByIndex;

    private FloydWarshall floydWarshall;

    /**
     * Crea la red de confianza con los valores indicados.
     *
     * @param weightConnections Valores de las conexiones entre los elementos.
     *
     * @throws IllegalArgumentException Si la estructura de valores de confianza
     * es nula.
     */
    public WeightedGraph(Map<Node, Map<Node, Number>> weightConnections) {

        validateConnections(weightConnections);

        nodesIndex = makeIndex(weightConnections);
        nodesByIndex = makeNodesByIndex(nodesIndex);

        adjMatrixEdgeWeightedDigraph = makeWeightedDiGraph(weightConnections);
        floydWarshall = null;

        validateWeightsGraph(adjMatrixEdgeWeightedDigraph);
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
        validateWeightMatrix(matrix);
        nodesIndex = makeIndex(ordering);
        nodesByIndex = makeNodesByIndex(nodesIndex);

        adjMatrixEdgeWeightedDigraph = makeWeightedDiGraph(nodesIndex, matrix);

        validateWeightsGraph(adjMatrixEdgeWeightedDigraph);
    }

    public WeightedGraph(Collection<Node> nodes, Set<PathBetweenNodes<Node>> edges) {
        validateEdges(edges);

        nodesIndex = makeIndex(nodes.stream().sorted().collect(Collectors.toList()));
        nodesByIndex = makeNodesByIndex(nodesIndex);

        double[][] matrix = makeMatrixFromEdges(edges);

        adjMatrixEdgeWeightedDigraph = makeWeightedDiGraph(nodesIndex, matrix);

        validateWeightsGraph(adjMatrixEdgeWeightedDigraph);
    }

    private double[][] makeMatrixFromEdges(Set<PathBetweenNodes<Node>> edges) {
        double[][] matrix = new double[nodesIndex.size()][nodesIndex.size()];
        edges.parallelStream()
                .filter(edge -> Double.isFinite(edge.getLength()))
                .forEach(edge -> {
                    Integer fromIndex = nodesIndex.get(edge.from());
                    Integer toIndex = nodesIndex.get(edge.to());
                    double weight = Objects.equals(fromIndex, toIndex) ? 0 : edge.getFirstWeight();
                    matrix[fromIndex][toIndex] = weight;
                });

        IntStream.range(0, matrix.length).boxed().parallel().forEach(index -> matrix[index][index] = 1);
        return matrix;
    }

    private void validateEdges(Set<PathBetweenNodes<Node>> edges) throws IllegalArgumentException {
        List<PathBetweenNodes<Node>> edgesWithMoreThanOneJump = edges.parallelStream()
                .filter(edge -> edge.numEdges() != 1)
                .collect(Collectors.toList());

        if (!edgesWithMoreThanOneJump.isEmpty()) {
            System.out.println("There are edges that are paths!");
            edgesWithMoreThanOneJump.forEach(edge -> System.out.println(edge));
            throw new IllegalArgumentException("The edges specified have more than one jump: " + edgesWithMoreThanOneJump.toString());
        }
    }

    public Optional<Double> connectionWeight(Node node1, Node node2) {
        Optional<Double> weight = getEdge(node1, node2).map(edge -> edge.weight());
        return weight;
    }

    /**
     * Devuelve la intensidad de la conexión directa entre dos nodos.
     *
     * @param node1
     * @param node2
     * @return
     */
    private Optional<DirectedEdge> getEdge(Node node1, Node node2) {
        int indexNode1 = nodesIndex.get(node1);
        int indexNode2 = nodesIndex.get(node2);

        List<DirectedEdge> edgesFromNode1 = new ArrayList<>();
        final Iterable<DirectedEdge> directedEdges = adjMatrixEdgeWeightedDigraph.adj(indexNode1);
        for (DirectedEdge a : directedEdges) {
            if (a.weight() > 0) {
                edgesFromNode1.add(a);
            }
        }

        Optional<DirectedEdge> edgeNode1ToNode2 = edgesFromNode1.parallelStream()
                .filter(edge -> ((edge.from() == indexNode1) && (edge.to() == indexNode2)))
                .findAny();

        return edgeNode1ToNode2;
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

    public double distance(Node node1, Node node2) {
        initFloydWarshall();

        int indexNode1 = nodesIndex.get(node1);
        int indexNode2 = nodesIndex.get(node2);
        double distance = floydWarshall.dist(indexNode1, indexNode2);
        return distance;
    }

    private synchronized void initFloydWarshall() {
        if (floydWarshall == null) {
            AdjMatrixEdgeWeightedDigraph distanceGaph = inverseOfEdgeValue(adjMatrixEdgeWeightedDigraph);
            floydWarshall = new FloydWarshall(distanceGaph);
        }
    }

    public Optional<PathBetweenNodes<Node>> shortestPath(Node node1, Node node2) {
        initFloydWarshall();

        int indexNode1 = nodesIndex.get(node1);
        int indexNode2 = nodesIndex.get(node2);

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

            PathBetweenNodes<Node> pathBetweenNodes = new PathBetweenNodes<>(this, pathNodesIncludingStartAndEnd);
            return Optional.of(pathBetweenNodes);
        } else {
            return Optional.empty();
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

        IntStream.range(0, nodesSorted.size()).parallel().boxed().forEach(indexRow -> {
            Node node1 = nodesSorted.get(indexRow);

            IntStream.range(0, nodesSorted.size()).parallel().boxed().forEach(indexColumn -> {
                Node node2 = nodesSorted.get(indexColumn);
                double value = connectionWeight(node1, node2).orElse(0.0);
                matrix[indexRow][indexColumn] = value;
            });
        });

        return matrix;
    }

    public double[][] asMatrixUnboxed() {

        final List<Node> nodesSorted = nodesSortingForMatrix();

        double[][] matrix = new double[nodesSorted.size()][nodesSorted.size()];

        IntStream.range(0, nodesSorted.size()).parallel().boxed().forEach(indexRow -> {
            Node node1 = nodesSorted.get(indexRow);

            IntStream.range(0, nodesSorted.size()).parallel().boxed().forEach(indexColumn -> {
                Node node2 = nodesSorted.get(indexColumn);
                double value = connectionWeight(node1, node2).orElse(0.0);
                matrix[indexRow][indexColumn] = value;
            });
        });

        validateWeightMatrix(matrix);
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
        return getTextTable(this.allNodes().stream().collect(Collectors.toSet()));
    }

    public TextTable getTextTable(Set<Node> nodes) {
        validateParameters(nodes);

        List<String> columnNames = new ArrayList<>();
        columnNames.add("node\\node");
        final List<Node> sortedNodes = this.allNodes().stream().sorted().filter(node -> nodes.contains(node)).collect(Collectors.toList());
        Object[][] data = new Object[sortedNodes.size()][sortedNodes.size() + 1];
        columnNames.addAll(sortedNodes.stream().map(node -> node.toString() + " ").collect(Collectors.toList()));
        DecimalFormat format = new DecimalFormat("0.0000");
        for (int node1index = 0; node1index < sortedNodes.size(); node1index++) {
            Node node1 = sortedNodes.get(node1index);
            int row = node1index;

            data[row][0] = node1.toString();

            for (int node2index = 0; node2index < sortedNodes.size(); node2index++) {
                Node node2 = sortedNodes.get(node2index);
                int column = node2index + 1;

                String cellValue = getEdge(node1, node2)
                        .map(edge -> edge.weight())
                        .filter(weight -> weight > 0)
                        .map(weight -> format.format(weight))
                        .orElse("0");
                data[row][column] = cellValue + " ";
            }
        }
        TextTable textTable = new TextTable(columnNames.toArray(new String[0]), data);
        return textTable;
    }

    void validateParameters(Collection<Node> nodes) throws IllegalArgumentException {
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
        AdjMatrixEdgeWeightedDigraph copy = new AdjMatrixEdgeWeightedDigraph(adjMatrixEdgeWeightedDigraph.V());

        IntStream.range(0, adjMatrixEdgeWeightedDigraph.V()).sequential().forEach(vertex -> {
            for (DirectedEdge directedEdge : adjMatrixEdgeWeightedDigraph.adj(vertex)) {
                copy.addEdge(directedEdge);
            }

        });

        return copy;
    }

    protected final Map<Node, Integer> makeIndex(Map<Node, Map<Node, Number>> connections) {
        validateConnections(connections);
        List<Node> sortedNodes = connections.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        return makeIndex(sortedNodes);
    }

    protected final Map<Node, Integer> makeIndex(List<Node> nodes) {
        return IntStream.range(0, nodes.size()).boxed()
                .collect(Collectors.toMap(
                        indexNode -> nodes.get(indexNode),
                        indexNode -> indexNode)
                );
    }

    protected final AdjMatrixEdgeWeightedDigraph makeWeightedDiGraph(Map<Node, Map<Node, Number>> connections) {

        validateConnections(connections);

        AdjMatrixEdgeWeightedDigraph adjMatrixEdgeWeightedDigraph1 = new AdjMatrixEdgeWeightedDigraph(nodesIndex.size());

        connections.entrySet().forEach(entry -> {

            Node node1 = entry.getKey();
            int indexNode1 = nodesIndex.get(node1);
            Map<Node, Number> thisNodeConnections = entry.getValue();

            thisNodeConnections.entrySet().forEach(entry2 -> {
                Node node2 = entry2.getKey();
                int indexNode2 = nodesIndex.get(node2);
                double weight = entry2.getValue().doubleValue();

                final DirectedEdge directedEdge = new DirectedEdge(indexNode1, indexNode2, weight);

                if (directedEdge.weight() != weight) {
                    System.out.println("What happen here, nobody come.");
                }
                adjMatrixEdgeWeightedDigraph1.addEdge(directedEdge);

            });
        });

        return adjMatrixEdgeWeightedDigraph1;
    }

    private static <Node> AdjMatrixEdgeWeightedDigraph makeWeightedDiGraph(Map<Node, Integer> nodesIndex, double[][] weightMatrix) {

        validateWeightMatrix(weightMatrix);

        AdjMatrixEdgeWeightedDigraph adjMatrixEdgeWeightedDigraph = new AdjMatrixEdgeWeightedDigraph(nodesIndex.size());

        nodesIndex.keySet().stream().forEach(node1 -> {
            int indexNode1 = nodesIndex.get(node1);
            nodesIndex.keySet().stream().forEach(node2 -> {
                int indexNode2 = nodesIndex.get(node2);
                double weight = weightMatrix[indexNode1][indexNode2];
                adjMatrixEdgeWeightedDigraph.addEdge(new DirectedEdge(indexNode1, indexNode2, weight));
            });
        });

        return adjMatrixEdgeWeightedDigraph;
    }

    private void validateConnections(Map<Node, Map<Node, Number>> connections) throws IllegalStateException {
        List<Double> wrongValues = connections.values().parallelStream()
                .flatMap(thisNodeConnections -> thisNodeConnections.values().stream())
                .map(numberValue -> numberValue.doubleValue())
                .filter(value -> (value > 1) || (value < 0)).collect(Collectors.toList());

        if (!wrongValues.isEmpty()) {
            throw new IllegalStateException("Value must be given in [0,1] interval and it was: " + wrongValues.toString());
        }
    }

    protected final Map< Integer, Node> makeNodesByIndex(Map<Node, Integer> nodesIndex) {
        return nodesIndex.entrySet().parallelStream().collect(Collectors.toMap(
                entry -> entry.getValue(),
                entry -> entry.getKey()));
    }

    public double distanceJumpLimited(Node node1, Node node2, int maxjumps) {
        final double distance = this.shortestPath(node1, node2)
                .filter(path -> path.numJumps() > maxjumps)
                .map(path -> path.getLength())
                .orElse(Double.NaN);
        return distance;

    }

    private static void validateWeightMatrix(double[][] weightMatrix) {

        List<Double> wrongValues = IntStream.range(0, weightMatrix.length).boxed().map(row -> weightMatrix[row])
                .flatMap(row -> IntStream.range(0, row.length).boxed().map(collumn -> row[collumn]))
                .filter(value -> (value > 1.0) || (value < 0.0)).collect(Collectors.toList());

        if (!wrongValues.isEmpty()) {
            throw new IllegalStateException("Value must be given in [0,1] interval and it was: " + wrongValues.toString());
        }
    }

    private static void validateWeightsGraph(AdjMatrixEdgeWeightedDigraph adjMatrixEdgeWeightedDigraph) {

        List<DirectedEdge> allEdges = IntStream.range(0, adjMatrixEdgeWeightedDigraph.V()).boxed().parallel()
                .map(vertex -> {
                    Iterable<DirectedEdge> iterator = adjMatrixEdgeWeightedDigraph.adj(vertex);
                    ArrayList<DirectedEdge> listOfEdges = new ArrayList<>();
                    for (DirectedEdge edge : iterator) {
                        listOfEdges.add(edge);
                    }
                    return listOfEdges;
                })
                .flatMap(listOfEdges -> listOfEdges.parallelStream()).collect(Collectors.toList());

        List<DirectedEdge> badEdges = allEdges.parallelStream()
                .filter(edge -> (edge.weight() < 0) || (edge.weight() > 1))
                .collect(Collectors.toList());

        if (!badEdges.isEmpty()) {
            System.out.println("List of bad edges:");
            badEdges.forEach(edge -> System.out.println("\t" + edge));
            throw new IllegalStateException("arg");
        }

    }

    public static final AdjMatrixEdgeWeightedDigraph inverseOfEdgeValue(AdjMatrixEdgeWeightedDigraph distanceGraph) {

        AdjMatrixEdgeWeightedDigraph weightGraph = new AdjMatrixEdgeWeightedDigraph(distanceGraph.V());

        List<DirectedEdge> allEdges = IntStream.range(0, distanceGraph.V()).boxed()
                .map(vertex -> {
                    Iterable<DirectedEdge> iterator = distanceGraph.adj(vertex);
                    ArrayList<DirectedEdge> listOfEdges = new ArrayList<>();
                    for (DirectedEdge edge : iterator) {
                        listOfEdges.add(edge);
                    }
                    return listOfEdges;
                })
                .flatMap(listOfEdges -> listOfEdges.stream()).collect(Collectors.toList());

        List<DirectedEdge> allEdgesConverted = allEdges.stream()
                .map(edge -> {
                    final double weight = edge.weight();

                    double distance = 1 / weight;

                    if (weight == 0) {
                        distance = Double.POSITIVE_INFINITY;
                    }

                    return new DirectedEdge(edge.from(), edge.to(), distance);
                }).collect(Collectors.toList());

        allEdgesConverted.forEach(edge -> weightGraph.addEdge(edge));
        return weightGraph;
    }

    public String toPairwiseDistancesTable() {
        return toPairwiseDistancesTable(this.nodesIndex.keySet());
    }

    public String toPairwiseDistancesTable(Set<Node> nodes) {
        TextTable textTable = getPairwiseDistancesTable(nodes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        textTable.printTable(recordingStream, 0);

        return baos.toString();
    }

    public TextTable getPairwiseDistancesTable(Collection<Node> nodes) {

        initFloydWarshall();
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

                double dist = floydWarshall.dist(node1index, node2index);
                String cellValue = format.format(dist) + " ";
                data[row][column] = cellValue;
            }
        }
        TextTable textTable = new TextTable(columnNames.toArray(new String[0]), data);
        return textTable;
    }

    public WeightedGraph<Node> getSubGraph(Collection<Node> nodes) {
        Map<Node, Map<Node, Number>> edgesOfSubGraph = getSubGraphEdges(nodes);

        WeightedGraph<Node> subGraph = new WeightedGraph<>(edgesOfSubGraph);
        return subGraph;
    }

    private Map<Node, Map<Node, Number>> getSubGraphEdges(Collection<Node> nodes) {
        Map<Node, Map<Node, Number>> edgesOfSubGraph = nodes.parallelStream().collect(Collectors.toMap(node1 -> node1, node1 -> {
            Map<Node, Number> edgesFromThisVertex = nodes.parallelStream()
                    .filter(node2 -> this.connectionWeight(node1, node2).isPresent())
                    .collect(Collectors.toMap(
                            node2 -> node2,
                            node2 -> {
                                return this.connectionWeight(node1, node2).get();
                            }));

            return edgesFromThisVertex;
        }));
        return edgesOfSubGraph;
    }

    public Collection<PathBetweenNodes<Node>> getEdgesFromNode(Node node1) {

        List<PathBetweenNodes<Node>> edgesFromNode = this.allNodes().parallelStream()
                .map(node2 -> this.getEdge(node1, node2))
                .filter(edge -> edge.isPresent())
                .map(edge -> edge.get())
                .filter(edge -> edge.from() != edge.to())
                .map(edge -> {
                    Node node2 = nodesByIndex.get(edge.to());
                    return new PathBetweenNodes<>(this, Arrays.asList(node1, node2));
                })
                .collect(Collectors.toList());

        return edgesFromNode;
    }

    public Set<PathBetweenNodes<Node>> allEdges() {

        Set<PathBetweenNodes<Node>> allEdges = this.allNodes().parallelStream()
                .flatMap(node1 -> {

                    Collection<PathBetweenNodes<Node>> edgesFromNode = this.getEdgesFromNode(node1);

                    if (edgesFromNode.isEmpty()) {
                        return new ArrayList<PathBetweenNodes<Node>>().parallelStream();
                    } else {
                        return edgesFromNode.parallelStream();
                    }
                })
                .filter(path -> path.isEdge())
                .filter(path -> !path.isSelf())
                .collect(Collectors.toSet());

        return allEdges;
    }

    @Override
    public int compareTo(WeightedGraph<Node> o) {
        List<Node> allNodes = this.allNodes().stream().sorted().collect(Collectors.toList());
        List<Node> allNodesOther = o.allNodes().stream().sorted().collect(Collectors.toList());

        int compareNatural = StringsOrderings.compareNatural(allNodes.toString(), allNodesOther.toString());

        if (compareNatural != 0) {
            return compareNatural;
        } else {
            return Integer.compare(this.hashCode(), o.hashCode());
        }
    }

    public String printPairwiseDistancesTable() {
        TextTable textTable = getPairwiseDistancesTable(this.allNodes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        textTable.printTable(recordingStream, 0);

        return baos.toString();
    }

}
