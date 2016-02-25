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
package delfos.factories;

import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.ArmonicAggregationOfTwoValues;
import delfos.common.aggregationoperators.EnsureDegreeOfFairness;
import delfos.common.aggregationoperators.GeometricMean;
import delfos.common.aggregationoperators.HarmonicMean;
import delfos.common.aggregationoperators.HeronianMeanAggregationOfTwoValues;
import delfos.common.aggregationoperators.MaximumValue;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.Median;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.aggregationoperators.Mode;
import delfos.common.aggregationoperators.RMSMean;
import delfos.common.aggregationoperators.TwoValuesAggregator;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Factoría de operaciones de agregación. Conoce todos los operadores de
 * agregación de esta biblioteca y es capaz de recuperar por nombre.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class AggregationOperatorFactory extends Factory<TwoValuesAggregator> {

    private static final AggregationOperatorFactory instance;

    public static AggregationOperatorFactory getInstance() {
        return instance;
    }

    static {
        instance = new AggregationOperatorFactory();

        //Limit aggregations.
        instance.addClass(MaximumValue.class);
        instance.addClass(MinimumValue.class);

        //Mean aggregations
        instance.addClass(Mean.class);
        instance.addClass(HarmonicMean.class);
        instance.addClass(GeometricMean.class);
        instance.addClass(RMSMean.class);

        //Other
        instance.addClass(Median.class);
        instance.addClass(Mode.class);
        instance.addClass(EnsureDegreeOfFairness.class);

        //Two values aggregators.
        instance.addClass(ArmonicAggregationOfTwoValues.class);
        instance.addClass(HeronianMeanAggregationOfTwoValues.class);

    }

    private AggregationOperatorFactory() {
    }

    /**
     * Devuelve todos los operadores de agregación generales, es decir, que
     * agregan cualquier número positivo de valores.
     *
     * @return Operadores de agregación generales.
     */
    public Collection<AggregationOperator> getAllAggregationOperators() {
        ArrayList<AggregationOperator> ret = new ArrayList<>();
        for (Class<? extends TwoValuesAggregator> c : allClasses.values()) {
            try {
                if (AggregationOperator.class.isAssignableFrom(c)) {
                    AggregationOperator aggregationOperator = (AggregationOperator) c.newInstance();
                    ret.add(aggregationOperator);
                }
            } catch (Throwable ex) {
                exceptionInCreation(c, ex);
            }
        }
        return ret;
    }
}
