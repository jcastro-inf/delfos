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
public class GeometricMeanConnectionWeightCentrality extends CentralityConceptDefinition<Integer> {

    public GeometricMeanConnectionWeightCentrality() {
    }

    @Override
    public double centrality(WeightedGraphAdapter<Integer> weightedGraph, Integer node) {

        double centralityThisMember = 1;
        for (Integer otherNode : weightedGraph.allNodes()) {

            if (node == otherNode) {
                // No se tiene en cuenta la confianza consigo mismo.
            } else {
                double connection = weightedGraph.connection(node, otherNode).doubleValue();
                centralityThisMember *= connection;
            }
        }
        return centralityThisMember;
    }

    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.CENTRALITY_CONCEPT_DEFINITION;
    }

}
