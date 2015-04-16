package delfos.common.aggregationoperators.penalty.functions;

import java.util.ArrayList;
import java.util.Map;

/**
 * Definición inicial del penalty, por Laura De Miguel. Es la sumatoria del
 * cuadrado del error acumulado en cada item.
 *
 * @version 17-julio-2014
* @author Jorge Castro Gallardo
 */
public class PenaltyInitialFunction extends PenaltyFunction {

    @Override
    public double penalty(
            Map<Integer, Map<Integer, Number>> referenceValues,
            Map<Integer, Number> aggregatedValues) {

        double penalty = 0;

        for (int idItem : referenceValues.keySet()) {
            double aggregatedValue = aggregatedValues.get(idItem).doubleValue();
            ArrayList<Number> referenceValuesThisItem = new ArrayList<>(referenceValues.get(idItem).values());
            double penaltyThisItem = penaltyThisItem(aggregatedValue, referenceValuesThisItem);
            penalty += Math.pow(penaltyThisItem, 2);
        }

        return penalty;
    }

    @Override
    public double penaltyThisItem(Number aggregatedValue, Iterable<Number> referenceValues) {

        double penaltyThisItem = 0;
        for (Number value : referenceValues) {
            double referenceValue = value.doubleValue();

            penaltyThisItem += Math.abs(aggregatedValue.doubleValue() - referenceValue);
        }

        return penaltyThisItem;
    }
}
