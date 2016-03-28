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

import delfos.common.Global;
import delfos.common.fuzzylabels.FuzzyLabel;
import delfos.dataset.util.DatasetPrinter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Normaliza un grafo ponderado dado. Utiliza la normalización
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class StrongTermOverConnections {

    private static final long serialVersionUID = 1L;

    public static final <Node> WeightedGraph<Node> applyStrongTerm(WeightedGraph<Node> source, double aStrong, double bStrong) {

        if (Global.isVerboseAnnoying()) {
            String printWeightedGraph = DatasetPrinter.printWeightedGraph(source);
            Global.showInfoMessage(printWeightedGraph);
        }

        Map<Node, Map<Node, Number>> connections;
        FuzzyLabel strong = FuzzyLabel.createAscendentLabel(aStrong, bStrong);

        connections = source.allNodes().parallelStream().collect(Collectors.toMap(nodeSource -> nodeSource, nodeSource -> {

            Map<Node, Number> thisNodeConnections;
            thisNodeConnections = source.allNodes().parallelStream().collect(Collectors.toMap(nodeDestiny -> nodeDestiny, nodeDestiny -> {
                Number weight = 0;
                if (nodeSource.equals(nodeDestiny)) {
                    //Skip same node connections by setting to 1.
                    weight = 1.0;
                } else {
                    //Do the normalisation.
                    final double originalConnection = source.connectionWeight(nodeSource, nodeDestiny).orElse(0.0);

                    if (Double.isFinite(originalConnection) && originalConnection > 0) {
                        final double modifiedConnection = strong.alphaCut(originalConnection);

                        weight = modifiedConnection;
                    }

                }
                return weight;
            }));
            return thisNodeConnections;
        }));

        WeightedGraph<Node> ret = new WeightedGraph<>(connections);

        if (Global.isVerboseAnnoying()) {
            String printNormalisedGraph = ret.toStringTable();
            Global.showInfoMessage(printNormalisedGraph);
        }

        return ret;

    }
}
