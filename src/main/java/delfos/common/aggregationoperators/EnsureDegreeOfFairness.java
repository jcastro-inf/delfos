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

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.common.statisticalfuncions.StandardDeviation;

/**
 * Operador de agregación que ensure some degree of fairness for the
 * aggregation.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public class EnsureDegreeOfFairness extends AggregationOperator {

    private final static long serialVersionUID = 1L;
    /**
     * Degree of fairness for the aggregation.
     */
    public static final Parameter DEGREE_OF_FAIRNESS = new Parameter(
            "DEGREE_OF_FAIRNESS",
            new DoubleParameter(0f, 10f, 0.1f),
            "Degree of fairness for the aggregation.");

    public EnsureDegreeOfFairness() {
        super();
        addParameter(DEGREE_OF_FAIRNESS);
    }

    public EnsureDegreeOfFairness(double degreeOfFairness) {
        this();
        setParameterValue(DEGREE_OF_FAIRNESS, degreeOfFairness);
    }

    @Override
    public double aggregateValues(Iterable<Number> values) {

        MeanIterative mean = new MeanIterative();
        StandardDeviation stdDev = new StandardDeviation();
        for (Number value : values) {
            mean.addValue(value.doubleValue());
            stdDev.addValue(value);

        }
        double aggregateValue = mean.getMean() - getDegreeOfFairness() * stdDev.getStandardDeviation();
        return (double) aggregateValue;
    }

    private double getDegreeOfFairness() {
        return (Double) getParameterValue(DEGREE_OF_FAIRNESS);
    }
}
