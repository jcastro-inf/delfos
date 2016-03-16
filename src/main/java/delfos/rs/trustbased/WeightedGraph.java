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

import dnl.utils.text.table.TextTable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.output.WriterOutputStream;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 18-jul-2013
 * @param <Node>
 */
public interface WeightedGraph<Node> extends Serializable {

    /**
     * Devuelve todos los nodos del grafo.
     *
     * @return
     */
    Collection<Node> allNodes();

    /**
     * Devuelve la centralidad de un nodo en el grafo, utilizando k conexiones.
     *
     * @param node Nodo para el que se calcula la centralidad.
     * @param k Longitud de los caminos que se considera.
     * @return Valor de centralidad del nodo.
     */
    double centrality(Node node, int k);

    /**
     * Devuelve la intensidad de la conexión compuesta entre dos nodos.
     *
     * @param id1
     * @param id2
     * @param k Longitud del camino.
     * @return
     */
    double composition(Node id1, Node id2, int k);

    /**
     * Devuelve la intensidad de la conexión directa entre dos nodos.
     *
     * @param id1 Nodo origen.
     * @param id2 Nodo destino.
     * @return Intensidad de la conexión entre dos nodos. Siempre devuelve un
     * número concreto, si no existe conexión devuelve cero.
     */
    public Number connection(Node id1, Node id2);

    double geodesicDistance(Node n1, Node n2);

    /**
     * Longitud máxima de un camino sin repetir nodos.
     *
     * @return
     */
    int maxK();

    public default Double[][] asMatrix() {

        final List<Node> nodesSorted = nodesSortingForMatrix();

        Double[][] matrix = new Double[nodesSorted.size()][nodesSorted.size()];

        for (int indexRow = 0; indexRow < nodesSorted.size(); indexRow++) {
            Node node = nodesSorted.get(indexRow);

            for (int indexColumn = 0; indexColumn < nodesSorted.size(); indexColumn++) {
                Node node2 = nodesSorted.get(indexColumn);
                double value = connection(node, node2).doubleValue();
                matrix[indexRow][indexColumn] = value;
            }
        }

        return matrix;
    }

    public default double[][] asMatrixUnboxed() {

        final List<Node> nodesSorted = nodesSortingForMatrix();

        double[][] matrix = new double[nodesSorted.size()][nodesSorted.size()];

        for (int indexRow = 0; indexRow < nodesSorted.size(); indexRow++) {
            Node node = nodesSorted.get(indexRow);

            for (int indexColumn = 0; indexColumn < nodesSorted.size(); indexColumn++) {
                Node node2 = nodesSorted.get(indexColumn);
                double value = connection(node, node2).doubleValue();
                matrix[indexRow][indexColumn] = value;
            }
        }

        return matrix;
    }

    public default List<Node> nodesSortingForMatrix() {
        List<Node> nodesSorted = allNodes().stream().sorted().collect(Collectors.toList());
        return Collections.unmodifiableList(nodesSorted);
    }

    public default void printTable(PrintStream outputStream) {
        TextTable textTable = getTextTable();
        textTable.printTable(outputStream, 0);
    }

    public default TextTable getTextTable() {
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

                double connection = this.connection(node1, node2).doubleValue();
                data[row][column] = format.format(connection);
            }
        }
        TextTable textTable = new TextTable(columnNames.toArray(new String[0]), data);
        return textTable;
    }

    public default TextTable getTextTable(Set<Node> nodes) {
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

                double connection = this.connection(node1, node2).doubleValue();
                data[row][column] = format.format(connection);
            }
        }
        TextTable textTable = new TextTable(columnNames.toArray(new String[0]), data);
        return textTable;
    }

    default void validateParameters(Set<Node> nodes) throws IllegalArgumentException {
        boolean allMatch = nodes.parallelStream().allMatch(node -> this.allNodes().contains(node));
        if (!allMatch) {
            throw new IllegalArgumentException("Specified nodes are not present in the weighted graph");
        }
    }

    public default void printTable(WriterOutputStream outputStream) {
        printTable(new PrintStream(outputStream));
    }

    public default String toStringTable() {
        TextTable textTable = getTextTable();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        textTable.printTable(recordingStream, 0);

        return baos.toString();
    }

    public default String toStringTable(Set<Node> nodes) {
        TextTable textTable = getTextTable(nodes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        textTable.printTable(recordingStream, 0);

        return baos.toString();
    }
}
