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
package delfos.similaritymeasures;

import java.util.Iterator;
import java.util.List;

/**
 * Distancia de grado 3, similar a la distancia euclidea pero usando las
 * potencias al cubo en vez de al cuadrado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class Distance3Degree extends BasicSimilarityMeasureAdapter {

    private static final long serialVersionUID = 1L;

    @Override
    public double similarity(List<Double> v1, List<Double> v2) {
        double sum = 0;
        Iterator<Double> i1 = v1.listIterator();
        Iterator<Double> i2 = v2.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            sum += Math.abs(Math.pow(i1.next() - i2.next(), 3));
        }
        sum = 1 / (1 + Math.pow(sum, 3));
        return (double) sum;
    }

    @Override
    public double weightedSimilarity(List<Double> v1, List<Double> v2, List<Double> weights) {
        double sum = 0;
        Iterator<Double> i1 = v1.listIterator();
        Iterator<Double> i2 = v2.listIterator();
        Iterator<Double> w = weights.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            sum += w.next() * Math.pow(i1.next() - i2.next(), 3);
        }
        sum = 1 / (1 + Math.pow(sum, 3));
        return (double) sum;
    }
}
