package delfos.common.aggregationoperators;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Operador de agregación que devuelve el valor mediano de los valores de
 * entrada
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public class Median extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {
        ArrayList<Number> list = new ArrayList<>();
        for (Number value : values) {
            list.add(value);
        }

        Collections.sort(list, (Number o1, Number o2) -> Float.compare(o1.floatValue(), o2.floatValue()));

        if (hasPairSize(list)) {
            float centralValue1 = list.get(list.size() / 2 - 1).floatValue();
            float centralValue2 = list.get(list.size() / 2).floatValue();
            return (centralValue1 + centralValue2) / 2;
        } else {
            float centralValue = list.get(list.size() / 2).floatValue();
            return centralValue;
        }
    }

    private boolean hasPairSize(ArrayList<Number> list) {
        return list.size() % 2 == 0;
    }
}
