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

import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 18-jul-2013
 */
public interface WeightedGraph<Node> extends Serializable {

    /**
     * Devuelve todos los nodos del grafo.
     *
     * @return
     */
    Collection<Node> allNodes();

    /**
     * Devuelve la centralidad de un nodo en el grafo, utilizando k conexiones.
     *
     * @param node Nodo para el que se calcula la centralidad.
     * @param k Longitud de los caminos que se considera.
     * @return Valor de centralidad del nodo.
     */
    double centrality(Node node, int k);

    /**
     * Devuelve la intensidad de la conexión compuesta entre dos nodos.
     *
     * @param id1
     * @param id2
     * @param k Longitud del camino.
     * @return
     */
    double composition(Node id1, Node id2, int k);

    /**
     * Devuelve la intensidad de la conexión directa entre dos nodos.
     *
     * @param id1 Nodo origen.
     * @param id2 Nodo destino.
     * @return Intensidad de la conexión entre dos nodos. Siempre devuelve un número concreto, si no existe conexión devuelve cero.
     */
    public Number connection(Node id1, Node id2);

    double geodesicDistance(Node n1, Node n2);

    /**
     * Longitud máxima de un camino sin repetir nodos.
     *
     * @return
     */
    int maxK();

}
