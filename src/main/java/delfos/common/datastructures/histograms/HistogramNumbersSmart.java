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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 24-feb-2014
 */
public class HistogramNumbersSmart {

    private final double binWidth;

    private int infiniteValues = 0;
    private int nanValues = 0;
    List<Double> values = new ArrayList<>();

    private double min = Integer.MAX_VALUE;
    private double max = Integer.MIN_VALUE;
    private int numValuesAdded = 0;

    public HistogramNumbersSmart(double binWidth) {
        this.binWidth = binWidth;
    }

    public HistogramNumbersSmart(double min, double max, double binWidth) {
        this.min = min;
        this.max = max;
        this.binWidth = binWidth;
    }

    private int getBin(double value) {

        if (value == min) {
            return 0;
        }

        if (value == max) {
            int numBins = (int) ((max - min) / binWidth);
            numBins--;
            return numBins;
        }

        int indexCubeta = 0;
        double _min = min;
        double _max = min + binWidth;
        while (true) {
            if (value >= _min && value < _max) {
                break;
            }

            _min += binWidth;
            _max += binWidth;
            indexCubeta++;
        }

        return indexCubeta;

    }

    public void addValue(double value) {
        if (Double.isNaN(value)) {
            nanValues++;
        } else if (Double.isInfinite(value)) {
            infiniteValues++;
        } else {
            min = Math.min(min, value);
            max = Math.max(max, value);

            values.add(value);
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

    @Override
    public String toString() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        printHistogram(recordingStream);

        return baos.toString();
    }

    public void printHistogram(PrintStream stream) {

        int numDecimals = lastSignificativeDecimal(binWidth);

        final String format;
        switch (numDecimals) {
            case 0:
                format = "0.0";
                break;
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

        int numberOfBins = (int) Math.ceil((max - min) / binWidth);
        if (numberOfBins == 0) {
            numberOfBins++;
        }

        double[] binValue = new double[numberOfBins];
        for (double value : values) {
            int bin = getBin(value);
            try {
                binValue[bin]++;
            } catch (ArrayIndexOutOfBoundsException ex) {
                if (bin == binValue.length) {
                    binValue[bin - 1]++;
                }
            }
        }

        String[] binName = new String[numberOfBins];
        DecimalFormat df = new DecimalFormat(format, new DecimalFormatSymbols(Locale.ENGLISH));
        //GENERA LAS ETIQUETAS DE LOS RANGOS DEL EJE X
        double rangeMin = min;
        double rangeMax = min + binWidth;

        for (int i = 0; i < numberOfBins; i++) {
            String thisBinName = "[" + df.format(rangeMin) + " " + df.format(rangeMax);
            String upperBound = ")";
            if (i == numberOfBins - 1) {
                upperBound = "]";
            }

            binName[i] = thisBinName + upperBound;
            rangeMin += binWidth;
            rangeMax += binWidth;
        }

        stream.println("Histogram of [" + min + "," + max + "] with " + numberOfBins + " bins");

        double minimumValue = new ArrayList<>(values).stream().sorted().collect(Collectors.toList()).get(0);
        double maximumValue = new ArrayList<>(values).stream().sorted().collect(Collectors.toList()).get(values.size() - 1);

        stream.println("Minimum = " + minimumValue);
        stream.println("Maximum = " + maximumValue);
        for (int i = 0; i < numberOfBins; i++) {
            stream.println(i + "\t" + binName[i] + "\t" + binValue[i]);
        }

        stream.println("spec\tNaN\t" + nanValues);
        stream.println("spec\tInfinity\t" + infiniteValues);
    }

    public void printHistogram(Writer stream) throws IOException {

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

        int numberOfBins = (int) Math.ceil((max - min) / binWidth);
        if (numberOfBins == 0) {
            numberOfBins++;
        }

        double[] binValue = new double[numberOfBins];
        for (double value : values) {
            int bin = getBin(value);
            binValue[bin]++;
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

        final double minThisBin = getBin_minBound(index);
        final double maxThisBin = getBin_maxBound(index);

        int numValues = 0;
        for (double value : values) {
            if (Double.isFinite(value)) {
                if (minThisBin <= value && value < maxThisBin) {
                    numValues++;
                }
            }
        }

        return numValues;
    }

    public int getNumBins() {
        int numberOfBins = (int) Math.ceil((max - min) / binWidth);
        if (numberOfBins == 0) {
            numberOfBins++;
        }

        return numberOfBins;
    }

    public double getBin_minBound(int index) {
        double rangeMin = min;
        double rangeMax = min + binWidth;

        for (int i = 0; i < index; i++) {
            rangeMin += binWidth;
            rangeMax += binWidth;
        }

        return rangeMin;
    }

    public double getBin_maxBound(int index) {
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
