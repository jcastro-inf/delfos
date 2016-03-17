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
import java.util.TreeMap;

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

        Map<Node, Map<Node, Number>> connections = new TreeMap<>();
        FuzzyLabel strong = FuzzyLabel.createAscendentLabel(aStrong, bStrong);

        for (Node nodeSource : source.allNodes()) {
            TreeMap<Node, Number> thisNodeConnections = new TreeMap<>();
            source.allNodes().stream().forEach((nodeDestiny) -> {
                if (nodeSource.equals(nodeDestiny)) {
                    //Skip same node connections by setting to 1.
                    thisNodeConnections.put(nodeDestiny, 1);
                } else {
                    //Do the normalisation.
                    final double originalConnection = source.connectionWeight(nodeSource, nodeDestiny);

                    if (Double.isFinite(originalConnection) && originalConnection > 0) {
                        final double modifiedConnection = strong.alphaCut(originalConnection);
                        thisNodeConnections.put(nodeDestiny, modifiedConnection);
                    }
                }
            });
            connections.put(nodeSource, thisNodeConnections);
        }

        WeightedGraph<Node> ret = new WeightedGraph<>(connections);

        if (Global.isVerboseAnnoying()) {
            String printNormalisedGraph = ret.toStringTable();
            Global.showInfoMessage(printNormalisedGraph);
        }

        return ret;

    }
}
