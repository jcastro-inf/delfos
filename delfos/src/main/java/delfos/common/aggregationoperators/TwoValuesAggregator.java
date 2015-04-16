package delfos.common.aggregationoperators;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Clase que provee los métodos para un operador de agregación que agrega dos
 * valores.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 01-Mar-2013
 */
public abstract class TwoValuesAggregator extends ParameterOwnerAdapter {

    /**
     * Método que agrega los dos valores especificados en un único valor.
     *
     * @param v1 Valor a agregar.
     * @param v2 Valor a agregar.
     * @return valor agregado de los valores especificados
     *
     * @throws IllegalArgumentException Si alguno de los parametros es null.
     */
    public abstract float aggregateTwoValues(Number v1, Number v2);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TwoValuesAggregator) {

            Class<? extends TwoValuesAggregator> thisName = this.getClass();
            TwoValuesAggregator twoValuesAggregator = (TwoValuesAggregator) obj;
            Class<? extends TwoValuesAggregator> compareName = twoValuesAggregator.getClass();
            if (thisName.equals(compareName)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = getName().hashCode();
        return hash;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.AGGREGATION_OPERATOR;

    }

}
