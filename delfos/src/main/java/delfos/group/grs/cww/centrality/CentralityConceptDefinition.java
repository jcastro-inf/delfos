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
package delfos.group.grs.cww.centrality;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.rs.trustbased.WeightedGraphAdapter;

/**
 * Interfaz que define los metodos del calculo de la centralidad de un usuario
 * en un grafo.
 *
* @author Jorge Castro Gallardo
 *
 * @version 4-Marzo-2014
 * @param <Node>
 */
public abstract class CentralityConceptDefinition<Node> extends ParameterOwnerAdapter {

    private static final long serialVersionUID = 1L;

    /**
     * "Especifica el sistema de recomendación single user que se extiende para
     * ser usado en recomendación a grupos.
     */
    public CentralityConceptDefinition() {
        super();
    }

    /**
     * Devuelve la centralidad en la red del nodo indicado.
     *
     * @param weightedGraphAdapter
     * @param node
     * @return
     */
    public abstract double centrality(WeightedGraphAdapter<Node> weightedGraphAdapter, Node node);
}
