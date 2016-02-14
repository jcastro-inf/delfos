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
package delfos.common.statisticalfuncions;

import java.util.Collection;

/**
 * Versión thread-safe de la clase {@link MeanIterative}, implementado como un
 * wrapper.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
