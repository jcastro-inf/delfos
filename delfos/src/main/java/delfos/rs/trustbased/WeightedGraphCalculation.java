package delfos.rs.trustbased;

import java.util.Collection;
import delfos.algorithm.AlgorithmAdapter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.ParameterOwnerType;

/**
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 *
 * @param <Node> Tipo de los nodos del grafo.
 * @version 1.0 03-Jun-2013
 */
public abstract class WeightedGraphCalculation<Node> extends AlgorithmAdapter {

    /**
     * Calcula un grafo ponderado a partir del conjunto de datos indicado,
     * teniendo en cuenta solo los
     *
     * @param datasetLoader Conjunto de datos del que se extrae el grafo con
     * ponderaciones.
     * @param users Usuarios para los que se calcula la red de confianza.
     * @return
     */
    public abstract WeightedGraphAdapter<Node> computeTrustValues(DatasetLoader<? extends Rating> datasetLoader, Collection<Integer> users) throws CannotLoadRatingsDataset;

    /**
     * Calcula el grafo difuso del dataset indicado como parámetro, tomando
     * todos los usuarios como nodos del mismo.
     *
     * @param datasetLoader
     * @return
     * @throws CannotLoadRatingsDataset
     */
    public WeightedGraphAdapter<Node> computeTrustValues(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        return computeTrustValues(datasetLoader, datasetLoader.getRatingsDataset().allUsers());
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.WEIGHTED_GRAPH_CALCULATION;
    }

}
