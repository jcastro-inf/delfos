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
package delfos.utils.fuzzyclustering.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import delfos.utils.fuzzyclustering.vector.DataVector;

/**
 *
 * @version 15-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class CosineDistance_noNull extends DistanceFunction {

    @Override
    public <Key> double distance(DataVector<Key> vector1, DataVector<Key> vector2) {

        Set<Key> intersection = new TreeSet<>();

        intersection.addAll(vector1.keySet());
        intersection.retainAll(vector2.keySet());

        List<Double> vector1intersected = new ArrayList<>(intersection.size());
        List<Double> vector2intersected = new ArrayList<>(intersection.size());

        for (Key element : intersection) {
            vector1intersected.add(vector1.get(element));
            vector2intersected.add(vector2.get(element));
        }

        double cosine = cosine(vector1intersected, vector2intersected);

        double distance = 1 / cosine - 1;
        return distance;
    }

    public double cosine(List<Double> v1, List<Double> v2) {

        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("The vector lengths are different");
        }

        double numerator = 0;
        double denominator1 = 0, denominator2 = 0;

        ListIterator<Double> i1 = v1.listIterator();
        ListIterator<Double> i2 = v2.listIterator();

        for (int i = 0; i < v1.size(); i++) {
            Double r1 = i1.next();
            Double r2 = i2.next();

            numerator = numerator + r1 * r2;
            denominator1 = denominator1 + r1 * r1;
            denominator2 = denominator2 + r2 * r2;
        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0;
        } else {
            float coseno = (float) (numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2)));
            return coseno;
        }

    }
}
