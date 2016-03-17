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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    }
}
