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
package delfos.common.aggregationoperators;

import delfos.common.datastructures.MultiSet;

/**
 * Operador de agregación que devuelve el valor más frecuente de los valores de
 * entrada
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public class Mode extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public double aggregateValues(Iterable<Number> values) {
        MultiSet<Double> ms = new MultiSet();
        for (Number value : values) {
            Double d = value.doubleValue();
            ms.add(d);
        }

        int maxFreq = 0;
        double moda = 0;

        for (Double o : ms.keySet()) {
            if (ms.getFreq(o) > maxFreq) {
                maxFreq = ms.getFreq(o);
                moda = o.doubleValue();
            }
        }
        return moda;
    }
}
