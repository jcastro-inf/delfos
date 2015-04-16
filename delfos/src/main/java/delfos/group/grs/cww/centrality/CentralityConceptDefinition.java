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
