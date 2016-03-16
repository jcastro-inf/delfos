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

import java.util.TreeMap;
import delfos.common.fuzzylabels.FuzzyLabel;
import delfos.common.Global;
import delfos.dataset.util.DatasetPrinter;

/**
 * Normaliza un grafo ponderado dado. Utiliza la normalización
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 15-ene-2014
 * @param <Node>
 */
public class StrongTermOverConnections<Node> extends WeightedGraph<Node> {

    private static final long serialVersionUID = 1L;

    public StrongTermOverConnections(WeightedGraph<Node> source, double aStrong, double bStrong) {
        super();

        if (Global.isVerboseAnnoying()) {
            String printWeightedGraph = DatasetPrinter.printWeightedGraph(source);
            Global.showInfoMessage(printWeightedGraph);
        }
        FuzzyLabel strong = FuzzyLabel.createAscendentLabel(aStrong, bStrong);
        allNodes.addAll(source.allNodes());
        for (Node nodeSource : source.allNodes()) {
            TreeMap<Node, Number> thisNodeConnections = new TreeMap<Node, Number>();
            for (Node nodeDestiny : source.allNodes()) {
                if (nodeSource.equals(nodeDestiny)) {
                    //Skip same node connections by setting to 1.
                    thisNodeConnections.put(nodeDestiny, 1);
                } else {
                    //Do the normalisation.
                    final double originalConnection = source.connection(nodeSource, nodeDestiny).doubleValue();
                    final double modifiedConnection = strong.alphaCut(originalConnection);
                    thisNodeConnections.put(nodeDestiny, modifiedConnection);
                }
            }
            this.connections.put(nodeSource, thisNodeConnections);
        }

        if (Global.isVerboseAnnoying()) {
            String printNormalisedGraph = DatasetPrinter.printWeightedGraph(this);
            Global.showInfoMessage(printNormalisedGraph);
        }

    }
}
