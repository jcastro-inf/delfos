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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.Median;
import delfos.common.aggregationoperators.RMSMean;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 *
 * @version 02-jul-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class PenaltyFunction extends ParameterOwnerAdapter {

    protected static List<AggregationOperator> allAggregationOperators;

    {
        LinkedList<AggregationOperator> aggregationOperators = new LinkedList<>();
        aggregationOperators.add(new Mean());
        aggregationOperators.add(new Median());
        aggregationOperators.add(new RMSMean());
        allAggregationOperators = Collections.unmodifiableList(aggregationOperators);
    }

    public abstract double penalty(Map<Integer, Map<Integer, Number>> referenceValues_byMember, Map<Integer, Number> aggregated_byItem);

    public abstract double penaltyThisItem(Number aggregatedValue, Iterable<Number> referenceValues);

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.PENALTY_FUNCION;
    }

    public List<AggregationOperator> getAllowedAggregations() {
        return allAggregationOperators;
    }
}
