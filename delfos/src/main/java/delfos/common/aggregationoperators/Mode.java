package delfos.common.aggregationoperators;

import delfos.common.datastructures.MultiSet;

/**
 * Operador de agregación que devuelve el valor más frecuente de los valores de
 * entrada
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public class Mode extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {
        MultiSet<Double> ms = new MultiSet();
        for (Number value : values) {
            Double d = value.doubleValue();
            ms.add(d);
        }

        int maxFreq = 0;
        float moda = 0;

        for (Double o : ms.keySet()) {
            if (ms.getFreq(o) > maxFreq) {
                maxFreq = ms.getFreq(o);
                moda = o.floatValue();
            }
        }
        return moda;
    }
}
