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
package delfos.common.aggregationoperators.penalty.functions;

import java.util.ArrayList;
import java.util.Map;

/**
 * Definición inicial del penalty, por Laura De Miguel. Es la sumatoria del
 * cuadrado del error acumulado en cada item.
 *
 * @version 17-julio-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
