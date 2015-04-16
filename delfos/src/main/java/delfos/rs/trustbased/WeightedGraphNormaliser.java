package delfos.rs.trustbased;

import java.util.TreeMap;
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
public class WeightedGraphNormaliser<Node> extends WeightedGraphAdapter<Node> {

    private static final long serialVersionUID = 1L;

    public WeightedGraphNormaliser(WeightedGraph<Node> source) {
        super();

        if (Global.isVerboseAnnoying()) {
            String printWeightedGraph = DatasetPrinter.printWeightedGraph(source);
            Global.showMessage(printWeightedGraph);
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (Node nodeSource : source.allNodes()) {
            for (Node nodeDestiny : source.allNodes()) {
                if (nodeSource.equals(nodeDestiny)) {
                    //Skip same node connections
                    continue;
                }
                double connectionValue = source.connection(nodeSource, nodeDestiny).doubleValue();
                min = Math.min(min, connectionValue);
                max = Math.max(max, connectionValue);
            }
        }

        Global.showMessage("min = " + min);
        Global.showMessage("max = " + max);

        if (Global.isVerboseAnnoying()) {
            Global.showMessage("Original range       [" + min + "," + max + "]\n");
            Global.showMessage("Normalising based on [0.0," + max + "]\n");
        }
        min = 0;
        if (max == 1 && min == 0) {
            Global.showWarning("Weighted graph normalisation isn't needed (Values were alrealdy normalised).");
        }

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
                    final double normalisedConnection = (originalConnection - min) / (max - min);
                    thisNodeConnections.put(nodeDestiny, normalisedConnection);
                }
            }
            this.connections.put(nodeSource, thisNodeConnections);
        }
        WeightedGraphAdapter<Node> normalisedGraph = new WeightedGraphAdapter<Node>(connections);

        if (Global.isVerboseAnnoying()) {
            String printNormalisedGraph = DatasetPrinter.printWeightedGraph(normalisedGraph);
            Global.showMessage(printNormalisedGraph);
        }

    }
}
