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
package delfos.common.aggregationoperators.dummy;

import delfos.common.aggregationoperators.AggregationOperator;
import java.util.Collection;

public class AnyAggregation extends AggregationOperator {

    @Override
    public double aggregateValues(Collection<? extends Number> values) {
        if (isTrivialCase(values)) {
            return values.iterator().next().doubleValue();
        } else {
            throw new IllegalStateException("Not a trivial case of aggregation " + values.toString());
        }
    }

    public static boolean isTrivialCase(Iterable<? extends Number> values) {
        int count = count(values);
        if (count == 1) {
            return true;
        }
        return allRatingsEqual(values);
    }

    private static int count(Iterable<? extends Number> values) {
        int count = 0;
        for (Number value : values) {
            count++;
        }
        return count;
    }

    private static boolean allRatingsEqual(Iterable<? extends Number> values) {
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
