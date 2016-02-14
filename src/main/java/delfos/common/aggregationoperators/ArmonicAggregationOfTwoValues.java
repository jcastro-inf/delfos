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

/**
 * Operador de agregación F1-Score, que agrega los valores indicados según la
 * siguiente fórmula:
 *
 * <p>
 * <p>
 * aggregatedValue = (2*v1*v2)/(v1+v2)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 01-Mar-2013
 */
public class ArmonicAggregationOfTwoValues extends TwoValuesAggregator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateTwoValues(Number v1, Number v2) {
        double d1 = v1.doubleValue();
        double d2 = v2.doubleValue();

        if (d1 < 0) {
            throw new IllegalArgumentException("v1 es menor que cero.");
        }
        if (d1 > 1) {
            throw new IllegalArgumentException("v1 es mayor que uno.");
        }
        if (d2 < 0) {
            throw new IllegalArgumentException("v2 es menor que cero.");
        }
        if (d2 > 1) {
            throw new IllegalArgumentException("v2 es mayor que uno.");
        }
        return (float) ((2 * d1 * d2) / (d1 + d2));
    }
}
