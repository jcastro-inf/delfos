package delfos.common.statisticalfuncions;

import java.util.Collection;
import delfos.common.Global;

/**
 * Media calculada sin almacenar los valores previos. Se pueden ir añadiendo
 * valores a la media y se reajusta automáticamente.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 21-01-2013
 * @version 1.0 03/04/2012
 */
public class MeanIterative {

    /**
     * Valor actual de la media.
     */
    private double mean = Double.NaN;
    /**
     * Número de valores que comprende la media.
     */
    private long numValues = 0;
    /**
     * Numero de valores máximos que la media retiene, es decir, valor máximo de
     * la variable numValues. De esta manera es una media en la que tienen mayor
     * peso los n últimos valores.
     */
    private final Integer maxValues;

    /**
     * Constructor de una media sin valores. No se aplica número máximo de
     * valores.
     */
    public MeanIterative() {
        maxValues = null;
    }

    /**
     * Devuelve una media que indica la media de los últimos
     * <code>maxValues</code> almacenados
     *
     * @param maxValues
     */
    public MeanIterative(int maxValues) {
        this.maxValues = maxValues;
    }

    /**
     * Crea una media a partir de un conjunto de valores dado.
     *
     * @param values Valores para los que se calcula la media.
     */
    public MeanIterative(Collection<? extends Number> values) {
        if (!values.isEmpty() && numValues == 0) {
            mean = 0;
        }
        for (Number n : values) {
            addValue(n.floatValue());
        }
        maxValues = null;
    }

    /**
     * Obtiene el valor medio de los valores que se añadieron.
     *
     * @return Devuelve la media actual de los valores añadidos a la misma
     * @see MeanIterative#addValue(double)
     * @see
     * MeanIterative#addMean(delfos.common.StatisticalFuncions.MeanIterative)
     */
    public double getMean() {
        return mean;
    }

    /**
     * añade un valor a la media ya calculada, actualizando el valor de la media
     * según el número de valores que haya recibido previamente.
     *
     * @param value valor que se añade a la serie de valores que conforman la
     * media
     */
    public void addValue(double value) {
        if (numValues == 0) {
            mean = 0;
        }

        if (Double.isNaN(value)) {
            IllegalStateException ex = new IllegalStateException("Parameter is NaN.");
            Global.showWarning(ex.getMessage());
            Global.showError(ex);
        } else {
            if (Double.isInfinite(value)) {
                IllegalStateException ex = new IllegalStateException("Parameter is infinite, check for overflows.");
                Global.showWarning(ex.getMessage());
                Global.showError(ex);
            } else {
                double newMean = mean * (((double) numValues) / (numValues + 1.0)) + value / (numValues + 1);
                //double newMean = ( mean * numValues + value) / (numValues+1);
                if (Double.isNaN(newMean) || Double.isInfinite(newMean)) {
                    IllegalStateException ex = new IllegalStateException("Mean overflowed.");
                    Global.showWarning(ex.getMessage());
                    Global.showError(ex);
                } else {
                    numValues++;
                    mean = newMean;
                }
                if (maxValues != null && numValues > maxValues) {
                    numValues = maxValues;
                }

            }
        }
    }

    public void addMean(MeanIterative newMean) {
        if (newMean.numValues == 0) {
        } else if (this.numValues == 0) {
            this.numValues = newMean.numValues;
            this.mean = newMean.mean;
        } else {

            long incrementedNumValues = this.numValues + newMean.numValues;

            double weightThis = (double) (this.numValues) / incrementedNumValues;
            double weightNew = (double) (newMean.numValues) / incrementedNumValues;
            double newMeanValue = this.mean * weightThis + newMean.mean * weightNew;

            this.mean = newMeanValue;
            this.numValues = incrementedNumValues;
        }
    }

    /**
     * Combina dos medias, de manera que se preserva la media como si se hubiera
     * utilizado una sola en principio, es decir, considera cada individuo, no
     * el valor en el momento actual de la media a agregar como un único
     * individuo
     *
     * @param newMean Media que se añade
     * @return Media que combina la media anterior (this) y la nueva media.
     */
    public MeanIterative getCombinedMean(MeanIterative newMean) {
        MeanIterative combinedMean = new MeanIterative();

        combinedMean.addMean(this);
        combinedMean.addMean(newMean);

        return combinedMean;
    }

    /**
     * Devuelve el número de valores que se añadieron a la media. Sirve para
     * combinar varias medias, de manera que queden ponderadas correctamente
     *
     * @return Número de valores que se tienen en cuenta para el cálculo de la
     * media
     */
    public long getNumValues() {
        return numValues;
    }

    /**
     * Método toString que representa este objeto como el valor medio de los
     * valores que tiene y entre paréntesis muestra el número de valores que
     * generan dicho valor medio.
     *
     * @return Representación amigable del contenido del objeto.
     */
    @Override
    public String toString() {
        return "mean = " + getMean() + "(" + getNumValues() + ")";
    }

    /**
     * Añade un conjunto de valores a la media.
     *
     * @param values Conjunto de valores que se añaden.
     */
    public void addValues(Collection<? extends Number> values) {
        values.stream().forEach((n) -> {
            addValue(n.floatValue());
        });
    }

    public boolean isEmpty() {
        return getNumValues() == 0;
    }
}
