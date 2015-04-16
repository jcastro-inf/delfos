package delfos.group.grs.cww.centrality.definitions;

import java.util.ArrayList;
import java.util.List;
import delfos.common.parameters.ParameterOwnerType;
import delfos.group.grs.cww.centrality.CentralityConceptDefinition;
import delfos.rs.trustbased.PathBetweenNodes;
import delfos.rs.trustbased.WeightedGraphAdapter;

/**
 *
* @author Jorge Castro Gallardo
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
