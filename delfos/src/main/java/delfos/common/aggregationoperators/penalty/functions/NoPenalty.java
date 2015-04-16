package delfos.common.aggregationoperators.penalty.functions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;

/**
 * Para usar agregaciones simples, sin penalti.
 *
* @author Jorge Castro Gallardo
 */
public class NoPenalty extends PenaltyFunction {

    public static final Parameter AGGREGATION = new Parameter("AGGREGATION", new ParameterOwnerRestriction(AggregationOperator.class, new Mean()));
    private AggregationOperator oldAggregationOperator = new Mean();

    private List<AggregationOperator> aggregationOperators = null;

    public NoPenalty() {
        super();
        addParameter(AGGREGATION);

        addParammeterListener(() -> {
            AggregationOperator newAggregationOperator = (AggregationOperator) getParameterValue(AGGREGATION);

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = this.getClass().getSimpleName()
                    + "_" + oldAggregationOperator.getAlias();

            String newAliasNewParameters
                    = this.getClass().getSimpleName()
                    + "_" + newAggregationOperator.getAlias();

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldAggregationOperator = newAggregationOperator;
                setAlias(newAliasNewParameters);
            }
        });

        addParammeterListener(() -> {
            AggregationOperator aggregationOperator = (AggregationOperator) getParameterValue(AGGREGATION);
            aggregationOperators = new LinkedList<>();
            aggregationOperators.add(aggregationOperator);
        });

        String oldAliasOldParameters
                = this.getClass().getSimpleName()
                + "_" + oldAggregationOperator.getAlias();
        setAlias(oldAliasOldParameters);
    }

    public NoPenalty(AggregationOperator aggregationOperator) {
        this();

        setParameterValue(AGGREGATION, aggregationOperator);
    }

    @Override
    public double penalty(
            Map<Integer, Map<Integer, Number>> referenceValues,
            Map<Integer, Number> aggregatedValues) {
        return 0;
    }

    @Override
    public double penaltyThisItem(Number aggregatedValue, Iterable<Number> referenceValues) {
        AggregationOperator aggregationOpperator = (AggregationOperator) getParameterValue(AGGREGATION);
        double aggregatedValueExpected = aggregationOpperator.aggregateValues(referenceValues);

        if (aggregatedValue.doubleValue() == aggregatedValueExpected) {
            return 0;
        } else {
            double expectedRounded = NumberRounder.round(aggregatedValueExpected, 5);
            double aggregatedRounded = NumberRounder.round(aggregatedValue.doubleValue(), 5);
            if (expectedRounded == aggregatedRounded) {
                return 0;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }
    }

    @Override
    public List<AggregationOperator> getAllowedAggregations() {
        return aggregationOperators;
    }
}
