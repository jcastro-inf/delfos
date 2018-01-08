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
import java.util.stream.IntStream;

/**
 * Medida de similitud basada en la distancia euclídea. Ha sido modificada para que 1 corresponda a similitud perfecta y
 * sea menor cuanto más diferente hasta 0. Ésto se ha conseguido sumandole 1 (para evitar división por cero) y
 * calculando la inversa. NOTA: fórmula extraída de Programming Collective Intelligence by Toby Segaran. Copyright 2007
 * Toby Segaran, 978-0-596-52932-1
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class EuclideanDistance extends BasicSimilarityMeasureAdapter {

    private static final long serialVersionUID = 1L;

    @Override
    public double similarity(List<Double> v1, List<Double> v2) {
        double sum = 0;
        Iterator<Double> i1 = v1.listIterator();
        Iterator<Double> i2 = v2.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            sum += Math.pow(i1.next() - i2.next(), 2);
        }
        sum = 1 / (1 + Math.sqrt(sum));
        return (double) sum;
    }

    @Override
    public double weightedSimilarity(List<Double> v1, List<Double> v2, List<Double> weights) {
        double sum = 0;
        Iterator<Double> i1 = v1.listIterator();
        Iterator<Double> i2 = v2.listIterator();
        Iterator<Double> w = weights.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            sum += w.next() * Math.pow(i1.next() - i2.next(), 2);
        }
        sum = 1 / (1 + Math.sqrt(sum));
        return (double) sum;
    }

    public double distance(List<Double> l1, List<Double> l2) {
        if (l1.size() != l2.size()) {
            throw new IllegalArgumentException("Vector lenghts are different");
        }

        double sum = IntStream.range(0, l1.size()).mapToDouble(i -> {
            Double v1 = l1.get(i);
            Double v2 = l2.get(i);
            return Math.pow(v1 - v2, 2);
        }).sum();

        double distance = Math.sqrt(sum);
        return distance;
    }
}
