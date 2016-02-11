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
package delfos.common.datastructures.histograms;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Histograma con precisión exacta.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 2-julio-2014
 */
public class HistogramSmart_bigDecimal {

    private final BigDecimal binWidth;

    private int infiniteValues = 0;
    private int nanValues = 0;
    List<BigDecimal> values = new ArrayList<>();

    private BigDecimal min = new BigDecimal(Integer.MAX_VALUE);
    private BigDecimal max = new BigDecimal(Integer.MIN_VALUE);
    private int numValuesAdded = 0;

    public HistogramSmart_bigDecimal(BigDecimal binWidth) {
        this.binWidth = binWidth;
    }

    private int getBin(BigDecimal value) {
        int indexCubeta = 0;

        BigDecimal _min = min;
        BigDecimal _max = min.add(binWidth);
        while (true) {
            if (value.compareTo(_min) >= 0 && value.compareTo(_max) < 0) {
                break;
            }

            _min = _min.add(binWidth);
            _max = _max.add(binWidth);
            indexCubeta++;
        }

        return indexCubeta;

    }

    public void addValueInfinite() {
        infiniteValues++;
        numValuesAdded++;
    }

    public void addValueNaN() {
        nanValues++;
        numValuesAdded++;
    }

    public void addValue(BigDecimal value) {

        min = min.min(value);
        max = max.max(value);

        values.add(value);

        numValuesAdded++;
    }

    public void addValue(double value) {
        if (Double.isNaN(value)) {
            addValueInfinite();
        } else if (Double.isInfinite(value)) {
            addValueNaN();
        } else {
            addValue(new BigDecimal(value));
        }
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

        int numDecimals = lastSignificativeDecimal(binWidth.doubleValue());

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

        int numberOfBins = max.subtract(min).divideAndRemainder(binWidth)[0].intValue();

        if (numberOfBins == 0) {
            numberOfBins++;
        }

        int[] binValue = new int[numberOfBins];
        for (BigDecimal value : values) {
            int bin = getBin(value);
            binValue[bin]++;
        }

        String[] binName = new String[numberOfBins];
        DecimalFormat df = new DecimalFormat(format, new DecimalFormatSymbols(Locale.ENGLISH));
        //GENERA LAS ETIQUETAS DE LOS RANGOS DEL EJE X
        BigDecimal rangeMin = min;
        BigDecimal rangeMax = min.add(binWidth);

        for (int i = 0; i < numberOfBins; i++) {
            binName[i] = "[" + df.format(rangeMin) + " " + df.format(rangeMax) + "]";
            rangeMin = rangeMin.add(binWidth);
            rangeMax = rangeMax.add(binWidth);
        }

        stream.println("Histogram of [" + min + "," + max + "] with " + numberOfBins + " bins");

        for (int i = 0; i < numberOfBins; i++) {
            stream.println(i + "\t" + binName[i] + "\t" + binValue[i]);
        }

        stream.println("spec\tNaN\t" + nanValues);
        stream.println("spec\tInfinity\t" + infiniteValues);
    }

    public void printHistogram(Writer stream) throws IOException {

        int numDecimals = lastSignificativeDecimal(binWidth.doubleValue());

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

        int numberOfBins = max.subtract(min).divideAndRemainder(binWidth)[0].intValue();

        if (numberOfBins == 0) {
            numberOfBins++;
        }

        int[] binValue = new int[numberOfBins];
        for (BigDecimal value : values) {
            int bin = getBin(value);
            binValue[bin]++;
        }

        String[] binName = new String[numberOfBins];
        DecimalFormat df = new DecimalFormat(format, new DecimalFormatSymbols(Locale.ENGLISH));
        //GENERA LAS ETIQUETAS DE LOS RANGOS DEL EJE X
        BigDecimal rangeMin = min;
        BigDecimal rangeMax = min.add(binWidth);

        for (int i = 0; i < numberOfBins; i++) {
            binName[i] = "[" + df.format(rangeMin) + " " + df.format(rangeMax) + "]";
            rangeMin = rangeMin.add(binWidth);
            rangeMax = rangeMax.add(binWidth);
        }

        stream.write("Histogram of [" + min + "," + max + "] with " + numberOfBins + " bins\n");

        for (int i = 0; i < numberOfBins; i++) {
            stream.write(i + "\t" + binName[i] + "\t" + binValue[i] + "\n");
        }

        stream.write("spec\tNaN\t" + nanValues + "\n");
        stream.write("spec\tInfinity\t" + infiniteValues + "\n");
    }

    public int getNumValues() {
        return numValuesAdded;
    }

    /**
     *
     * @param index De cero a numberOfBins.
     * @return Número de valores en ese bin.
     */
    public int getBin_numValues(int index) {

        final BigDecimal minThisBin = getBin_minBound(index);
        final BigDecimal maxThisBin = getBin_maxBound(index);

        int numValues = 0;
        for (BigDecimal value : values) {
            if (minThisBin.compareTo(value) >= 0 && value.compareTo(maxThisBin) < 0) {
                numValues++;
            }
        }

        return numValues;
    }

    public int getNumBins() {
        int numberOfBins = max.subtract(min).divideAndRemainder(binWidth)[0].intValue();

        if (numberOfBins == 0) {
            numberOfBins++;
        }

        return numberOfBins;
    }

    public BigDecimal getBin_minBound(int index) {
        BigDecimal rangeMin = min;
        BigDecimal rangeMax = min.add(binWidth);

        for (int i = 0; i < index; i++) {
            rangeMin = rangeMin.add(binWidth);
            rangeMax = rangeMax.add(binWidth);
        }

        return rangeMin;
    }

    public BigDecimal getBin_maxBound(int index) {
        BigDecimal rangeMin = min;
        BigDecimal rangeMax = min.add(binWidth);

        for (int i = 0; i < index; i++) {
            rangeMin = rangeMin.add(binWidth);
            rangeMax = rangeMax.add(binWidth);
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
