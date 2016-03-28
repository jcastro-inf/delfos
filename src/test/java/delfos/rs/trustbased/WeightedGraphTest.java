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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class WeightedGraphTest {

    public WeightedGraphTest() {
    }

    /**
     * Test of connection method, of class WeightedGraph.
     */
    @Test
    public void testConnection() {

        double[][] weightMatrix = {
            {1.0, 0.5, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.5, 0.0, 0.0},
            {0.0, 0.0, 1.0, 0.5, 0.0},
            {0.0, 0.0, 0.0, 1.0, 0.5},
            {0.1, 0.0, 0.0, 0.0, 1.0}
        };
        List<Integer> ordering = IntStream.rangeClosed(0, 4).boxed().sorted().collect(Collectors.toList());

        WeightedGraph<Integer> weightedGraph = new WeightedGraph<>(weightMatrix, ordering);

        System.out.println(weightedGraph.shortestPath(0, 4));
        System.out.println(weightedGraph.shortestPath(4, 3));

        System.out.println(weightedGraph);
    }

    /**
     * Test of connection method, of class WeightedGraph.
     */
    @Test
    public void testSubGraphComplete() {

        double[][] weightMatrix = {
            {1.0, 0.5, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.5, 0.0, 0.0},
            {0.0, 0.0, 1.0, 0.5, 0.0},
            {0.0, 0.0, 0.0, 1.0, 0.5},
            {0.1, 0.0, 0.0, 0.0, 1.0}
        };
        List<Integer> ordering = IntStream.rangeClosed(0, 4).boxed().sorted().collect(Collectors.toList());

        WeightedGraph<Integer> weightedGraph = new WeightedGraph<>(weightMatrix, ordering);

        WeightedGraph<Integer> subGraphComplete = weightedGraph.getSubGraph(weightedGraph.allNodes());

        Assert.assertEquals("The graphs must be equal", weightedGraph.toString(), subGraphComplete.toString());
    }

    /**
     * Test of connection method, of class WeightedGraph.
     */
    @Test
    public void testSubGraph() {

        double[][] weightMatrix = {
            {1.0, 0.5, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.5, 0.0, 0.0},
            {0.0, 0.0, 1.0, 0.5, 0.0},
            {0.0, 0.0, 0.0, 1.0, 0.5},
            {0.1, 0.0, 0.0, 0.0, 1.0}
        };
        List<Integer> ordering = IntStream.rangeClosed(0, 4).boxed().sorted().collect(Collectors.toList());

        WeightedGraph<Integer> weightedGraph = new WeightedGraph<>(weightMatrix, ordering);

        System.out.println(weightedGraph.shortestPath(0, 4));
        System.out.println(weightedGraph.shortestPath(4, 3));

        System.out.println("+++++++++ Original Graph ++++++++++++++++++++++++++++++++++++");
        System.out.println(weightedGraph);
        System.out.println(weightedGraph.toPairwiseDistancesTable());
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n\n");

        WeightedGraph<Integer> subGraphComplete = weightedGraph.getSubGraph(Arrays.asList(0, 1, 2));
        System.out.println("+++++++++ Sub Graph +++++++++++++++++++++++++++++++++++++++++");
        System.out.println(subGraphComplete);
        System.out.println(subGraphComplete.toPairwiseDistancesTable());
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("");

        Assert.assertTrue("There should be a path from node 2 to node 0 in the original", weightedGraph.shortestPath(2, 0).isPresent());
        Assert.assertTrue("There should not be a path from node 2 to node 0 in the subgraph", !subGraphComplete.shortestPath(2, 0).isPresent());
    }
}
