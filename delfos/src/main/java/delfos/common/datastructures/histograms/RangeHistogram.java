package delfos.common.datastructures.histograms;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 24-feb-2014
 */
public class RangeHistogram {

    private final double min;
    private final double max;

    private final double binWidth;
    private final int numberOfBins;

    private final int[] binValue;
    private int infiniteValues = 0;
    private int nanValues = 0;
    private int numValuesAdded = 0;

    public RangeHistogram(double min, double max, int numberOfBins) {
        this.min = min;
        this.max = max;
        this.numberOfBins = numberOfBins;

        if (min >= max) {
            throw new IllegalArgumentException("The min value cannot be greater than or equal to max value: [" + min + " - " + max + "]");
        }

        if (numberOfBins <= 0) {
            throw new IllegalArgumentException("Number of bins cannot be less than one [" + numberOfBins + "]");
        }

        binValue = new int[numberOfBins];
        binWidth = (max - min) / numberOfBins;
    }

    private int getBin(double value) {
        int indexCubeta = 0;

        double _min = min;
        double _max = min + binWidth;
        while (indexCubeta < numberOfBins) {
            if (value >= _min && value < _max) {
                break;
            }

            _min += binWidth;
            _max += binWidth;
            indexCubeta++;
        }

        if (indexCubeta == numberOfBins) {
            System.out.println("stop!");
        }

        return indexCubeta;

    }

    private final double delta = 0.0001;

    public void checkRange(double value) {
        if (value - delta > max || value + delta < min) {
            throw new IndexOutOfBoundsException("Value '" + value + "' out of range [" + min + "," + max + "] ( delta=" + delta + ")");
        }
    }

    public void addValue(double value) {
        checkRange(value);

        if (Double.isNaN(value)) {
            nanValues++;
        } else {
            if (Double.isInfinite(value)) {
                infiniteValues++;
            } else {
                binValue[getBin(value)]++;
            }
        }

        numValuesAdded++;

    }

    public int lastSignificativeDecimal(double value) {
        double log10 = Math.log10(value);

        final int retInt;
        if (log10 < 0) {
            final double ret;

            ret = (-log10);

            retInt = (int) Math.ceil(ret);
        } else {
            retInt = 0;
        }

        return retInt;
    }

    public void printHistogram(PrintStream stream) {

        int numDecimals = lastSignificativeDecimal(binWidth);

        final String format;
        switch (numDecimals) {
            case 1:
                format = "0.0";
                break;
            case 2:
                format = "0.00";
                break;
            case 3:
                format = "0.000";
                break;
            case 4:
                format = "0.0000";
                break;
            case 5:
                format = "0.00000";
                break;
            case 6:
                format = "0.000000";
                break;
            default:
                format = "Not supported";
                throw new IllegalArgumentException("Not supported.");
        }

        String[] binName = new String[numberOfBins];
        DecimalFormat df = new DecimalFormat(format, new DecimalFormatSymbols(Locale.ENGLISH));
        //GENERA LAS ETIQUETAS DE LOS RANGOS DEL EJE X
        double rangeMin = min;
        double rangeMax = min + binWidth;

        for (int i = 0; i < numberOfBins; i++) {
            binName[i] = "[" + df.format(rangeMin) + " " + df.format(rangeMax) + "]";
            rangeMin += binWidth;
            rangeMax += binWidth;
        }

        stream.println("Histogram of [" + min + "," + max + "] with " + numberOfBins + " bins");

        for (int i = 0; i < numberOfBins; i++) {
            stream.println(i + "\t" + binName[i] + "\t" + binValue[i]);
        }

        stream.println("spec\tNaN\t" + nanValues);
        stream.println("spec\tInfinity\t" + infiniteValues);
    }

    public int getNumValues() {
        return numValuesAdded;
    }

    /**
     *
     * @param index De cero a numberOfBins.
     * @return Número de valores en ese bin.
     */
    public int getHistogramAtBin(int index) {
        return binValue[index];
    }

    public double getBinMinBound(int index) {
        double rangeMin = min;
        double rangeMax = min + binWidth;

        for (int i = 0; i < index; i++) {
            rangeMin += binWidth;
            rangeMax += binWidth;
        }

        return rangeMin;
    }

    public double getBinMaxBound(int index) {
        double rangeMin = min;
        double rangeMax = min + binWidth;

        for (int i = 0; i < index; i++) {
            rangeMin += binWidth;
            rangeMax += binWidth;
        }

        return rangeMax;
    }

    public int getNanValues() {
        return nanValues;
    }

    public int getInfiniteValues() {
        return infiniteValues;
    }
}
