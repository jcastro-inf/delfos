package delfos.common.aggregationoperators;

import delfos.common.statisticalfuncions.MeanIterative;

/**
 * Operador de agregación de la media aritmética de los valores de entrada
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public class Mean extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {
        MeanIterative meanIterative = new MeanIterative();

        for (Number value : values) {
            meanIterative.addValue(value.doubleValue());
        }
        return (float) meanIterative.getMean();
    }
}
