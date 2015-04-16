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
public class AritmethicMeanConnectionWeightCentrality extends CentralityConceptDefinition<Integer> {

    public AritmethicMeanConnectionWeightCentrality() {
    }

    @Override
    public double centrality(WeightedGraphAdapter<Integer> weightedGraph, Integer node) {

        double centrality = 0;
        int n = 0;
        double sumOfConnections = 0;
        for (Integer otherNode : weightedGraph.allNodes()) {

            if (node == otherNode) {
                // No se tiene en cuenta la confianza consigo mismo.
            } else {
                double connection = weightedGraph.connection(node, otherNode).doubleValue();
                sumOfConnections += connection;
                n++;
            }
        }
        centrality = sumOfConnections / n;
        return centrality;
    }

    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.CENTRALITY_CONCEPT_DEFINITION;
    }

}
