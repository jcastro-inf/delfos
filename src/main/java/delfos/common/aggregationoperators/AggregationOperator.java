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
import java.util.Arrays;

/**
 * Clase abstracta que encapsula el funcionamiento de un método de agregación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    public abstract double aggregateValues(Iterable<Number> values);

    /**
     * Método que agrega los valores especificados en un único valor.
     *
     * @param values Conjunto de valores a agregar
     * @return valor agregado de los valores especificados
     *
     * @throws IllegalArgumentException Si no contiene ningún valor para agregar
     * o si es nulo.
     */
    public final double aggregateValues(Number... values) {
        return aggregateValues(Arrays.asList(values));
    }

    public final double aggregateValues(double... values) {
        ArrayList<Number> arrayList = new ArrayList<>(values.length);

        for (double value : values) {
            arrayList.add(value);
        }
        return aggregateValues(arrayList);
    }

    @Override
    public final double aggregateTwoValues(Number v1, Number v2) {
        return aggregateValues(v1, v2);
    }
}
