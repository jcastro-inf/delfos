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
package delfos.group.grs.penalty.grouper;

import delfos.utils.fuzzyclustering.distance.DistanceFunction;
import delfos.utils.fuzzyclustering.vector.DataVector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.similaritymeasures.BasicSimilarityMeasure;

public class DistanceFromSimilarity extends DistanceFunction {

    private final BasicSimilarityMeasure basicSimilarityMeasure;

    public DistanceFromSimilarity(BasicSimilarityMeasure basicSimilarityMeasure) {
        this.basicSimilarityMeasure = basicSimilarityMeasure;
    }

    @Override
    public <Key> double distance(DataVector<Key> vector1, DataVector<Key> vector2) {
        Set<Key> intersection = new TreeSet<>();

        intersection.addAll(vector1.keySet());
        intersection.retainAll(vector2.keySet());

        List<Float> vector1intersected = new ArrayList<>(intersection.size());
        List<Float> vector2intersected = new ArrayList<>(intersection.size());

        intersection.stream().map((element) -> {
            vector1intersected.add(vector1.get(element).floatValue());
            return element;
        }).forEach((element) -> {
            vector2intersected.add(vector2.get(element).floatValue());
        });

        double similarity;
        try {
            similarity = basicSimilarityMeasure.similarity(vector1intersected, vector2intersected);
            double distance = 1 / similarity - 1;
            return distance;
        } catch (CouldNotComputeSimilarity ex) {
            return Double.POSITIVE_INFINITY;
        }
    }

}
