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
package delfos.common.aggregationoperators.penalty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.RMSMean;
import delfos.common.aggregationoperators.dummy.AnyAggregation;
import delfos.common.aggregationoperators.penalty.functions.ErrorPenalty;
import delfos.common.aggregationoperators.penalty.functions.PenaltyFunction;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.factories.AggregationOperatorFactory;

/**
 * Operador de agregación de la media geométrica de los valores de entrada
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 16-julio-2014
 */
public class PenaltyAggregationAllAggregations extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    public static final Parameter PENALTY = new Parameter("PENALTY", new ParameterOwnerRestriction(PenaltyFunction.class, new ErrorPenalty(1.5f)));

    private PenaltyFunction oldPenaltyFunction = new ErrorPenalty(1.5);

    private static final ArrayList<AggregationOperator> allAggregationOperators = new ArrayList<>();

    static {
        allAggregationOperators.addAll(AggregationOperatorFactory.getInstance().getAllAggregationOperators());
        for (Iterator<AggregationOperator> it = allAggregationOperators.iterator(); it.hasNext();) {
            AggregationOperator agregationOperator = it.next();
            if (agregationOperator.getClass().equals(PenaltyAggregationAllAggregations.class)) {
                it.remove();
            }
        }

        allAggregationOperators.add(new Mean());
        allAggregationOperators.add(new RMSMean());
    }

    public PenaltyAggregationAllAggregations() {
        super();
        addParameter(PENALTY);

        addParammeterListener(() -> {
            PenaltyFunction newPenaltyFunction = (PenaltyFunction) getParameterValue(PENALTY);

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = PenaltyAggregationAllAggregations.class.getSimpleName()
                    + "_" + oldPenaltyFunction.getAlias();

            String newAliasNewParameters
                    = PenaltyAggregationAllAggregations.class.getSimpleName()
                    + "_" + newPenaltyFunction.getAlias();

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldPenaltyFunction = newPenaltyFunction;
                setAlias(newAliasNewParameters);
            }
        });

        setAlias(PenaltyAggregationAllAggregations.class.getSimpleName() + "_" + ((PenaltyFunction) getParameterValue(PENALTY)).getAlias());
    }

    public PenaltyAggregationAllAggregations(PenaltyFunction penaltyFunction) {
        this();
        setParameterValue(PENALTY, penaltyFunction);
    }

    @Override
    public float aggregateValues(Iterable<Number> values) {
        PenaltyFunction penaltyFunction = (PenaltyFunction) getParameterValue(PENALTY);
        PenaltyInfo penaltyInfo = getPenaltyInfo(values, penaltyFunction);

        if (penaltyInfo.isTrivialCase()) {
            return values.iterator().next().floatValue();
        } else {
            double bestAggregatedRating = penaltyInfo.bestAggregationOperator.aggregateValues(values);
            return (float) bestAggregatedRating;
        }
    }

    public static class PenaltyInfo {

        public final AggregationOperator bestAggregationOperator;
        public final List<AggregationOperator> tiedAggregations;

        public PenaltyInfo(AggregationOperator bestAggregationOperator) {
            this.bestAggregationOperator = bestAggregationOperator;
            tiedAggregations = Collections.EMPTY_LIST;
        }

        private PenaltyInfo(AggregationOperator bestAggregationOperator, List<AggregationOperator> tiedAggregations) {

            this.tiedAggregations = Collections.unmodifiableList(new ArrayList<>(tiedAggregations));
            this.bestAggregationOperator = bestAggregationOperator;
        }

        public boolean isTrivialCase() {
            return bestAggregationOperator instanceof AnyAggregation;
        }
    }

    class PenaltyAggregationValue {

        AggregationOperator aggregationOperator;
        double penaltyValue;
        double aggregatedRating;

        public PenaltyAggregationValue(AggregationOperator aggregationOperator, double penaltyValue, double aggregatedRating) {
            this.aggregationOperator = aggregationOperator;
            this.penaltyValue = penaltyValue;
            this.aggregatedRating = aggregatedRating;
        }

    }

    public PenaltyInfo getPenaltyInfo(Iterable<Number> values, PenaltyFunction penaltyFunction) {

        if (new AnyAggregation().isTrivialCase(values)) {
            return new PenaltyInfo(new AnyAggregation());
        }

        List<PenaltyAggregationValue> aggregationsByPenaltyValue = new ArrayList<>(allAggregationOperators.size());

        for (AggregationOperator aggregationOperator : allAggregationOperators) {

            double aggregatedRating = aggregationOperator.aggregateValues(values);
            double penalty = penaltyFunction.penaltyThisItem(aggregatedRating, values);

            aggregationsByPenaltyValue.add(new PenaltyAggregationValue(aggregationOperator, penalty, aggregatedRating));
        }

        Collections.sort(aggregationsByPenaltyValue, (PenaltyAggregationValue o1, PenaltyAggregationValue o2) -> {
            double diff = o1.penaltyValue - o2.penaltyValue;
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        });

        Iterator<PenaltyAggregationValue> it = aggregationsByPenaltyValue.iterator();
        PenaltyAggregationValue bestPenaltyAggregationValue = it.next();
        List<AggregationOperator> ties = new ArrayList<>();
        for (; it.hasNext();) {
            PenaltyAggregationValue penaltyAggregationValue = it.next();
            if (penaltyAggregationValue.penaltyValue == bestPenaltyAggregationValue.penaltyValue) {
                ties.add(penaltyAggregationValue.aggregationOperator);
            }
        }

        if (ties.isEmpty()) {
            return new PenaltyInfo(bestPenaltyAggregationValue.aggregationOperator);
        } else {
            return new PenaltyInfo(bestPenaltyAggregationValue.aggregationOperator, ties);
        }
    }

}
