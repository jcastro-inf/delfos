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
package delfos.group.grs.cww.centrality.definitions;

import delfos.common.parameters.ParameterOwnerType;
import delfos.group.grs.cww.centrality.CentralityConceptDefinition;
import delfos.rs.trustbased.PathBetweenNodes;
import delfos.rs.trustbased.WeightedGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 04-mar-2014
 */
public class BetweennessCentrality extends CentralityConceptDefinition<Integer> {

    public BetweennessCentrality() {
    }

    @Override
    public double centrality(WeightedGraph<Integer> weightedGraph, Integer node) {

        ArrayList<Integer> allNodesButThis = new ArrayList<>(weightedGraph.allNodes());
        allNodesButThis.remove(node);

        List<PathBetweenNodes<Integer>> allShortestPaths = weightedGraph.allNodes().stream()
                .flatMap(n1 -> weightedGraph
                        .allNodes().stream()
                        .filter(n2 -> n1 != n2)
                        .map(n2 -> weightedGraph.shortestPath(n1, n2))
                )
                .filter(path -> path.isPresent())
                .map(path -> path.get())
                .collect(Collectors.toList());

        double pathsWithNode = allShortestPaths.stream()
                .filter((path) -> (path.getNodes().contains(node)))
                .count();

        double centrality = pathsWithNode / allShortestPaths.size();
        return centrality;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.CENTRALITY_CONCEPT_DEFINITION;
    }

}
