package delfos.rs.trustbased;

import java.util.TreeMap;
import delfos.common.fuzzylabels.FuzzyLabel;
import delfos.common.Global;
import delfos.dataset.util.DatasetPrinter;

/**
 * Normaliza un grafo ponderado dado. Utiliza la normalización
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 15-ene-2014
 * @param <Node>
 */
public class StrongTermOverConnections<Node> extends WeightedGraphAdapter<Node> {

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
