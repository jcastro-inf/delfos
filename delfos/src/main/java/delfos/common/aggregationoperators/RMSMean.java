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
 * @version 17-julio-2014
 */
public class RMSMean extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {

        MathContext mathContext = new MathContext(32, RoundingMode.HALF_UP);
        BigDecimal sum = new BigDecimal(BigInteger.ZERO, mathContext);
        int n = 0;

        for (Number value : values) {
            BigDecimal valueB = new BigDecimal(value.doubleValue(), mathContext);
            sum = sum.add(valueB.multiply(valueB, mathContext), mathContext);
            n++;
        }

        BigDecimal meanSquare = sum.multiply(BigDecimal.ONE.divide(new BigDecimal(n), mathContext));

        double rootMeanSquare = Math.sqrt(meanSquare.doubleValue());
        return (float) rootMeanSquare;
    }
}
