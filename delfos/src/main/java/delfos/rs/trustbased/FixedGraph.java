package delfos.rs.trustbased;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import delfos.algorithm.Algorithm;
import delfos.algorithm.AlgorithmProgressListener;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.ERROR_CODES;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.StringParameter;

/**
 * Envoltura para evitar que se recalcule el grafo en solicitudes sobre el mismo
 * dataset.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 03-May-2013
 * @param <Node>
 */
public class FixedGraph<Node> extends WeightedGraphCalculation<Node> {

    private static final long serialVersionUID = -3387516993124229948L;
    /**
     * Algoritmo para calcular el grafo.
     */
    public static final Parameter weightedGraphCalculation = new Parameter(
            "weightedGraphCalculation",
            new ParameterOwnerRestriction(WeightedGraphCalculation.class, new ShambourLu_UserBasedImplicitTrustComputation()));
    /**
     * Nombre del fichero en el que se guarda/recupera el grafo calculado.
     */
    public static final Parameter FILE_NAME = new Parameter("fileName", new StringParameter("weightedGraph.graph"));
    private final Map<DatasetLoader, WeightedGraphAdapter<Node>> models = Collections.synchronizedMap(new TreeMap<DatasetLoader, WeightedGraphAdapter<Node>>());

    public FixedGraph() {
        super();
        addParameter(FILE_NAME);
        addParameter(weightedGraphCalculation);

        addParammeterListener(new ParameterListener() {
            @Override
            public void parameterChanged() {
                models.clear();
            }
        });
    }

    public FixedGraph(String fileName, WeightedGraphCalculation<Node> weightedGraphCalculation) {
        this();

        if (fileName == null) {
            throw new IllegalArgumentException("The fileName cannot be null.");
        }

        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("The fileName cannot be empty.");
        }

        if (weightedGraphCalculation == null) {
            throw new IllegalArgumentException("The weightedGraphCalculation cannot be null.");
        }

        if (weightedGraphCalculation instanceof FixedGraph) {
            throw new IllegalArgumentException("The calculation cannot be a " + FixedGraph.class);
        }

        setParameterValue(FixedGraph.FILE_NAME, fileName);
        setParameterValue(FixedGraph.weightedGraphCalculation, weightedGraphCalculation);
    }

    @Override
    public WeightedGraphAdapter<Node> computeTrustValues(DatasetLoader<? extends Rating> datasetLoader, Collection<Integer> users) throws CannotLoadRatingsDataset {
        synchronized (models) {
            if (!models.containsKey(datasetLoader)) {

                AlgorithmProgressListener listener = new AlgorithmProgressListener() {
                    @Override
                    public void progressChanged(Algorithm algorithm) {
                        fireProgressChanged(
                                algorithm.getProgressTask(),
                                algorithm.getProgressPercent(),
                                algorithm.getProgressRemainingTime());
                    }
                };

                try {
                    WeightedGraphAdapter<Node> weightedGraph = loadGraph(getFileName());
                    models.put(datasetLoader, weightedGraph);
                } catch (FailureInPersistence ex) {
                    Global.showMessage(new Date().toString() + ": Building graph.\n");
                    getWeightedGraphCalculation().addProgressListener(listener);
                    WeightedGraphAdapter<Node> weightedGraph = getWeightedGraphCalculation().computeTrustValues(datasetLoader);
                    getWeightedGraphCalculation().removeProgressListener(listener);
                    models.put(datasetLoader, weightedGraph);
                    Global.showMessage(new Date().toString() + ": Graph built.\n");
                    saveGraph(getFileName(), weightedGraph);
                    Global.showMessage(new Date().toString() + ": Graph saved in file " + getFileName() + "\n");
                }
            }
            return models.get(datasetLoader);
        }
    }

    public WeightedGraphCalculation<Node> getWeightedGraphCalculation() {
        return (WeightedGraphCalculation<Node>) getParameterValue(weightedGraphCalculation);
    }

    private void saveGraph(String fileName, WeightedGraphAdapter<Node> model) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(model);
            oos.close();

        } catch (Throwable ex) {
            Global.showWarning("The persistence for " + this.getClass() + " couldn't save the graph.");
            ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
        }
    }

    private WeightedGraphAdapter<Node> loadGraph(String fileName) throws FailureInPersistence {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
            WeightedGraphAdapter<Node> weightedGraph = (WeightedGraphAdapter<Node>) ois.readObject();
            ois.close();
            return weightedGraph;
        } catch (Throwable ex) {
            Global.showWarning("The persistence for " + this.getClass() + " couldn't load the graph.");
            throw new FailureInPersistence(ex);
        }
    }

    private String getFileName() {
        return (String) getParameterValue(FILE_NAME);
    }
}
