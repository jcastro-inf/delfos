package delfos.common.statisticalfuncions;

import java.util.Collection;

/**
 * Versión thread-safe de la clase {@link MeanIterative}, implementado como un
 * wrapper.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 19-Junio-2014
 */
public class MeanIterative_Synchronized extends MeanIterative {

    private final MeanIterative meanIterative;

    /**
     * Devuelve una media que indica la media de los últimos
     * <code>maxValues</code> almacenados
     *
     * @param maxValues
     */
    public MeanIterative_Synchronized(int maxValues) {
        meanIterative = new MeanIterative(maxValues);
    }

    /**
     * Constructor de una media sin valores. No se aplica número máximo de
     * valores.
     */
    public MeanIterative_Synchronized() {
        meanIterative = new MeanIterative();
    }

    /**
     * Constructor de una media thread safe a partir de otra.
     *
     * @param meanIterative
     */
    public MeanIterative_Synchronized(MeanIterative meanIterative) {
        this.meanIterative = meanIterative;
    }

    /**
     * Crea una media a partir de un conjunto de valores dado.
     *
     * @param values Valores para los que se calcula la media.
     */
    public MeanIterative_Synchronized(Collection<? extends Number> values) {
        meanIterative = new MeanIterative(values);
    }

    @Override
    public synchronized double getMean() {
        return meanIterative.getMean();
    }

    @Override
    public synchronized void addValue(double value) {
        meanIterative.addValue(value);
    }

    @Override
    public synchronized void addMean(MeanIterative newMean) {
        meanIterative.addMean(meanIterative);
    }

    @Override
    public synchronized long getNumValues() {
        return meanIterative.getNumValues();
    }

    @Override
    public synchronized String toString() {
        return meanIterative.toString();

    }

    @Override
    public synchronized void addValues(Collection<? extends Number> values) {
        meanIterative.addValues(values);
    }

    @Override
    public synchronized boolean isEmpty() {
        return meanIterative.isEmpty();
    }
}
