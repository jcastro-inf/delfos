package delfos.common.aggregationoperators;

import java.util.Arrays;

/**
 * Clase abstracta que encapsula el funcionamiento de un método de agregación
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public abstract class AggregationOperator extends TwoValuesAggregator {

    /**
     * Método que agrega los valores especificados en un único valor.
     *
     * @param values Conjunto de valores a agregar
     * @return valor agregado de los valores especificados
     *
     * @throws IllegalArgumentException Si no contiene ningún valor para agregar
     * o si es nulo.
     */
    public abstract float aggregateValues(Iterable<Number> values);

    /**
     * Método que agrega los valores especificados en un único valor.
     *
     * @param values Conjunto de valores a agregar
     * @return valor agregado de los valores especificados
     *
     * @throws IllegalArgumentException Si no contiene ningún valor para agregar
     * o si es nulo.
     */
    public final float aggregateValues(Number... values) {
        return aggregateValues(Arrays.asList(values));
    }

    @Override
    public final float aggregateTwoValues(Number v1, Number v2) {
        return aggregateValues(v1, v2);
    }
}
