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
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;

/**
 * Función penalty parametrizable.
 *
 * @version 19-julio-2014
* @author Jorge Castro Gallardo
 */
public class ErrorPenalty extends PenaltyFunction {

    public static final Parameter INNER_EXPONENT;
    public static final Parameter OUTTER_EXPONENT;

    static {
        INNER_EXPONENT = new Parameter("INNER_EXPONENT", new FloatParameter(0.00000001f, 100f, 1.5f));
        OUTTER_EXPONENT = new Parameter("OUTTER_EXPONENT", new FloatParameter(0.00000001f, 100f, 1f));
    }

    private double oldInnerExponent = 1;

    public ErrorPenalty() {
        super();
        addParameter(INNER_EXPONENT);
        addParameter(OUTTER_EXPONENT);

        addParammeterListener(() -> {

            double innerExponent = ((Number) getParameterValue(INNER_EXPONENT)).doubleValue();
            innerExponent = NumberRounder.round(innerExponent, 2);

            if (oldInnerExponent != innerExponent) {
                oldInnerExponent = innerExponent;
                setAlias(ErrorPenalty.class.getSimpleName() + "(in=" + innerExponent + ")");

            }
        });
    }

    public ErrorPenalty(double innerPenalty) {
        this();
        setParameterValue(INNER_EXPONENT, innerPenalty);

    }

    public ErrorPenalty(double innerPenalty, double outterPenalty) {
        this(innerPenalty);
        setParameterValue(OUTTER_EXPONENT, outterPenalty);
    }

    @Override
    public double penalty(
            Map<Integer, Map<Integer, Number>> referenceValues,
            Map<Integer, Number> aggregatedValues) {

        double penalty = 0;

        final float outterExponent = (Float) getParameterValue(OUTTER_EXPONENT);

        for (int idItem : referenceValues.keySet()) {
            double aggregatedValue = aggregatedValues.get(idItem).doubleValue();
            ArrayList<Number> referenceValuesThisItem = new ArrayList<>(referenceValues.get(idItem).values());

            double penaltyThisItem = penaltyThisItem(aggregatedValue, referenceValuesThisItem);

            penaltyThisItem = Math.pow(penaltyThisItem, outterExponent);
            penalty += penaltyThisItem;
        }

        return penalty;
    }

    @Override
    public double penaltyThisItem(Number aggregatedValue, Iterable<Number> referenceValues) {

        final double innerExponent = ((Number) getParameterValue(INNER_EXPONENT)).doubleValue();

        double penaltyThisItem = 0;
        for (Number value : referenceValues) {
            double referenceValue = value.doubleValue();

            double penaltyThisCell = Math.abs(aggregatedValue.doubleValue() - referenceValue);
            penaltyThisCell = Math.pow(penaltyThisCell, innerExponent);

            penaltyThisItem += penaltyThisCell;
        }

        return penaltyThisItem;
    }
}
