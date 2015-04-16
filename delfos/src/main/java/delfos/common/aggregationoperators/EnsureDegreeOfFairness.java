package delfos.common.aggregationoperators;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.common.statisticalfuncions.StandardDeviation;

/**
 * Operador de agregación que ensure some degree of fairness for the
 * aggregation.
 *
* @author Jorge Castro Gallardo
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
            new FloatParameter(0f, 10f, 0.1f),
            "Degree of fairness for the aggregation.");

    public EnsureDegreeOfFairness() {
        super();
        addParameter(DEGREE_OF_FAIRNESS);
    }

    public EnsureDegreeOfFairness(float degreeOfFairness) {
        this();
        setParameterValue(DEGREE_OF_FAIRNESS, degreeOfFairness);
    }

    @Override
    public float aggregateValues(Iterable<Number> values) {

        MeanIterative mean = new MeanIterative();
        StandardDeviation stdDev = new StandardDeviation();
        for (Number value : values) {
            mean.addValue(value.doubleValue());
            stdDev.addValue(value);

        }
        double aggregateValue = mean.getMean() - getDegreeOfFairness() * stdDev.getStandardDeviation();
        return (float) aggregateValue;
    }

    private float getDegreeOfFairness() {
        return (Float) getParameterValue(DEGREE_OF_FAIRNESS);
    }
}
