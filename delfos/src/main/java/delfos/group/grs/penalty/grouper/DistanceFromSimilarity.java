package delfos.group.grs.penalty.grouper;

import es.sinbad2.jcastro.fuzzyclustering.distance.DistanceFunction;
import es.sinbad2.jcastro.fuzzyclustering.vector.DataVector;
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
