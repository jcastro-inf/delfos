package delfos.common.aggregationoperators;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Operador de agregación de la media geométrica de los valores de entrada
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-julio-2014
 */
public class HarmonicMean extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {

        MathContext mathContext = new MathContext(32, RoundingMode.HALF_UP);
        BigDecimal sum = new BigDecimal(BigInteger.ZERO);
        int n = 0;

        for (Number value : values) {
            BigDecimal valueB = new BigDecimal(value.doubleValue());

            sum = sum.add(BigDecimal.ONE.divide(valueB, mathContext));
            n++;
        }

        double harmonicMean = new BigDecimal(n).divide(sum, mathContext).doubleValue();

        return (float) harmonicMean;
    }
}
