package delfos.group.grs.cww.centrality.definitions;

import delfos.common.parameters.ParameterOwnerType;
import delfos.group.grs.cww.centrality.CentralityConceptDefinition;
import delfos.rs.trustbased.WeightedGraphAdapter;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 04-mar-2014
 */
public class ClosenessCentrality extends CentralityConceptDefinition<Integer> {

    public ClosenessCentrality() {
    }

    @Override
    public double centrality(WeightedGraphAdapter<Integer> weightedGraph, Integer node) {

        double centrality, numerator = 0;
        int n = 0;
        for (Integer otherNode : weightedGraph.allNodes()) {
            if (node == otherNode) {
                // No se tiene en cuenta la confianza consigo mismo.
            } else {
                double distance = weightedGraph.distance(node, otherNode);

                if (Double.isInfinite(distance)) {

                } else {
                    if (distance == 0) {

                    } else {
                        numerator += 1 / distance;
                    }
                }

                n++;
            }
        }
        centrality = numerator / n;
        return centrality;
    }

    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.CENTRALITY_CONCEPT_DEFINITION;
    }

}
