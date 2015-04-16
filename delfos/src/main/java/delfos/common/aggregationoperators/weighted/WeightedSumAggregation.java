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
