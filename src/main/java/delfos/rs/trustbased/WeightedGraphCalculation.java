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

import delfos.algorithm.AlgorithmWithExecutionProgressDefault;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import java.util.Collection;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @param <Node> Tipo de los nodos del grafo.
 * @version 1.0 03-Jun-2013
 */
public abstract class WeightedGraphCalculation<Node> extends AlgorithmWithExecutionProgressDefault {

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

    public WeightedGraph<Node> computeWeightedGraphForItem(DatasetLoader<? extends Rating> datasetLoader,
            WeightedGraph<Node> weightedGraph,
            Collection<User> users,
            Item item) {
        return weightedGraph;
    }

    @Override
    public String toString() {
        return getAlias();
    }

}
