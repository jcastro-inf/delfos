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

import java.util.ArrayList;
import java.util.List;
import delfos.common.parameters.ParameterOwnerType;
import delfos.group.grs.cww.centrality.CentralityConceptDefinition;
import delfos.rs.trustbased.PathBetweenNodes;
import delfos.rs.trustbased.WeightedGraphAdapter;

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
    public double centrality(WeightedGraphAdapter<Integer> weightedGraph, Integer node) {
        List<PathBetweenNodes<Integer>> allShortestPaths = new ArrayList<PathBetweenNodes<Integer>>();
        ArrayList<Integer> allNodesButThis = new ArrayList<Integer>(weightedGraph.allNodes());
        allNodesButThis.remove(node);

        for (Integer n1 : allNodesButThis) {
            for (Integer n2 : allNodesButThis) {
                if (n1 != n2) {
                    PathBetweenNodes shortestPath = weightedGraph.shortestPath(n1, n2);
                    allShortestPaths.add(shortestPath);
                }
            }
        }

        double pathsWithNode = 0;

        for (PathBetweenNodes<Integer> path : allShortestPaths) {
            if (path.getNodes().contains(node)) {
                pathsWithNode++;
            }
        }

        double centrality = pathsWithNode / allShortestPaths.size();
        return centrality;
    }

    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.CENTRALITY_CONCEPT_DEFINITION;
    }

}
