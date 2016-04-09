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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Operador de agregación que devuelve el valor mediano de los valores de entrada
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public class Median extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public double aggregateValues(Collection<Number> values) {
        ArrayList<Number> list = new ArrayList<>();
        for (Number value : values) {
            list.add(value);
        }

        Collections.sort(list, (Number o1, Number o2) -> Double.compare(o1.doubleValue(), o2.doubleValue()));

        if (hasPairSize(list)) {
            double centralValue1 = list.get(list.size() / 2 - 1).doubleValue();
            double centralValue2 = list.get(list.size() / 2).doubleValue();
            return (centralValue1 + centralValue2) / 2;
        } else {
            double centralValue = list.get(list.size() / 2).doubleValue();
            return centralValue;
        }
    }

    private boolean hasPairSize(ArrayList<Number> list) {
        return list.size() % 2 == 0;
    }
}
