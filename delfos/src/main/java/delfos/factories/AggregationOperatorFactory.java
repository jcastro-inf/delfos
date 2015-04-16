package delfos.factories;

import java.util.ArrayList;
import java.util.Collection;
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
import delfos.common.aggregationoperators.penalty.PenaltyAggregationAllAggregations;
import delfos.common.aggregationoperators.penalty.PenaltyAggregation_MeanRMS;
import delfos.common.aggregationoperators.RMSMean;
import delfos.common.aggregationoperators.TwoValuesAggregator;

/**
 * Factoría de operaciones de agregación. Conoce todos los operadores de
 * agregación de esta biblioteca y es capaz de recuperar por nombre.
 *
* @author Jorge Castro Gallardo
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

        instance.addClass(PenaltyAggregation_MeanRMS.class);
        instance.addClass(PenaltyAggregationAllAggregations.class);

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
