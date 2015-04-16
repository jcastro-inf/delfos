package delfos.common.statisticalfuncions;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Clase para calcular la desviación típica de una colección de valores.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (05/11/2012)
 */
public class StandardDeviation {

    /**
     * Colección de valores que comprende el cálculo de la desviación típica.
     */
    protected Collection<Number> _values;

    /**
     * Almacena el último valor medio devuelto.
     */
    /**
     * Crea un objeto para calcular la desviación típica de un conjunto de
     * valores.
     */
    public StandardDeviation() {
        _values = new LinkedList<Number>();
    }

    /**
     * Crea un objeto para calcular la desviación típica y le asigna el conjunto
     * de valores indicado por parámetro.
     *
     * @param values Valores a los que se calcula la desviación típica.
     */
    public StandardDeviation(Collection<? extends Number> values) {
        _values = new LinkedList<Number>(values);
    }

    /**
     * Obtiene la desviación típica de los valores que contiene.
     *
     * @return Desviación típica
     */
    public double getStandardDeviation() {
        if (_values.isEmpty()) {
            return Double.NaN;
        }
        MeanIterative mean = new MeanIterative();

        for (Number n : _values) {
            mean.addValue(n.doubleValue());
        }
        double meanValue = mean.getMean();

        float numerador = 0;
        for (Number n : _values) {
            numerador += Math.pow(meanValue - n.floatValue(), 2);
        }
        return Math.sqrt(numerador / _values.size());
    }

    /**
     * Añade un valor para que sea tenido en cuenta en el cálcuo de la
     * desviación típica
     *
     * @param value valor que se añade a la serie de valores
     */
    public void addValue(Number value) {
        _values.add(value);
    }

    /**
     * Añade una colección de valores para que sean tenidos en cuenta en el
     * cálculo de la desviación típica
     *
     * @param values Coleccion de valores a tener en cuenta.
     */
    public void addValues(Collection<? extends Number> values) {
        this._values.addAll(values);
    }

    /**
     * Devuelve el número de valores que se están teniendo en cuenta para el
     * cálculo de la desviación típica
     *
     * @return Número de valores que se tienen en cuenta
     */
    public long getNumValues() {
        return _values.size();
    }

    @Override
    public String toString() {
        return "std-dev=" + getStandardDeviation() + " numVal=" + getNumValues();

    }
}
