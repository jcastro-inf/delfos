package delfos.common.aggregationoperators.dummy;

import delfos.common.aggregationoperators.AggregationOperator;

public class AnyAggregation extends AggregationOperator {

    @Override
    public float aggregateValues(Iterable<Number> values) {
        if (isTrivialCase(values)) {
            return values.iterator().next().floatValue();
        } else {
            throw new IllegalStateException("Not a trivial case of aggregation " + values.toString());
        }
    }

    public static boolean isTrivialCase(Iterable<Number> values) {
        int count = count(values);
        if (count == 1) {
            return true;
        }
        return allRatingsEqual(values);
    }

    private static int count(Iterable<Number> values) {
        int count = 0;
        for (Number value : values) {
            count++;
        }
        return count;
    }

    private static boolean allRatingsEqual(Iterable<Number> values) {
        for (Number value1 : values) {
            for (Number value2 : values) {
                if (!value1.equals(value2)) {
                    return false;
                }
            }
        }
        return true;
    }

}
