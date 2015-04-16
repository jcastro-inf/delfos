package delfos.common.aggregationoperators;

/**
 * Operador de agregación de la media geométrica de los valores de entrada
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-julio-2014
 */
public class GeometricMean extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {

        double multiplication = 1;
        int n = 0;

        for (Number value : values) {
            multiplication *= value.doubleValue();
            n++;
        }

        double geometricMean = Math.pow(multiplication, 1.0 / n);

        return (float) geometricMean;
    }
}
