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
package delfos.common.aggregationoperators.weighted;

import java.util.List;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;

/**
 * Implementa la técnica de agregación con ponderación de valores.
 *
 * @see WeightedSum
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 4-Julio-2013
 */
public class WeightedSumAggregation extends WeightedAggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(
            List<? extends Number> values,
            List<? extends Number> weights) {

        if (values.isEmpty()) {
            throw new IllegalArgumentException("The values list cannot be empty");
        }
        if (weights.isEmpty()) {
            throw new IllegalArgumentException("The weights list cannot be empty");
        }
        if (weights.size() != values.size()) {
            throw new IllegalArgumentException("The values list and weights lists have different size.");
        }
        double numerador = 0;
        double denominador = 0;
        for (int i = 0; i < values.size(); i++) {
            double weight = weights.get(i).doubleValue();
            if (weight < 0) {
                throw new IllegalArgumentException("The weights cannot be negative!");
            }
            numerador += values.get(i).doubleValue() * weight;
            denominador += weight;
        }
        if (denominador >= 1.001) {
            throw new IllegalArgumentException("The weights sum is greater than 1 (weights sum " + denominador);
        }
        if (denominador == 0) {
            throw new IllegalArgumentException("The weights sum is zero.");
        }
        double aggregateValue = numerador / denominador;
        return (float) aggregateValue;
    }
}
