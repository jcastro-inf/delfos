package delfos.common.aggregationoperators;

import java.util.Iterator;

/**
 * Operador de agregación que devuelve el valor máximo.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public class MaximumValue extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {
        Iterator<? extends Number> it = values.iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("No values given");
        }
        float max = it.next().floatValue();
        while (it.hasNext()) {
            Number value = it.next();
            if (value.floatValue() > max) {
                max = value.floatValue();
            }
        }
        return max;
    }
}
