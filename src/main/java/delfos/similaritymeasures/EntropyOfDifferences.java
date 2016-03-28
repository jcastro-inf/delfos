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

import delfos.common.datastructures.MultiSet;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class EntropyOfDifferences extends SimilarityMeasureAdapter implements CollaborativeSimilarityMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public double similarity(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) throws CouldNotComputeSimilarity {
        double intersectionSize = commonRatings.size();
        if (intersectionSize == 0) {
            return Double.NaN;
        }

        MultiSet<Double> frequencies = new MultiSet<>();

        OptionalDouble mean1 = commonRatings.parallelStream().mapToDouble(commonRating -> commonRating.getRating1()).average();
        OptionalDouble mean2 = commonRatings.parallelStream().mapToDouble(commonRating -> commonRating.getRating2()).average();

        List<Double> differences = commonRatings.parallelStream().map(commonRating -> {
            double value1 = commonRating.getRating1() - mean1.getAsDouble();
            double value2 = commonRating.getRating2() - mean2.getAsDouble();
            double difference = value1 - value2;
            difference = Math.abs(difference);
            return difference;
        }).sorted().collect(Collectors.toList());

        differences.forEach(difference -> frequencies.add(difference));

        double N = frequencies.getN();
        double entropy = -frequencies.keySet().stream().mapToDouble(difference -> {
            double frequency = frequencies.getFreq(difference);
            double probability = frequency / N;
            double log2probability = Math.log(probability) / Math.log(2);
            double thisDifferenceEntropy = probability * log2probability;
            return thisDifferenceEntropy;
        }).sum();

        double maxEntropy = Math.log(N) / Math.log(2);
        double similarity = 1 - entropy / maxEntropy;

        return similarity;
    }

    @Override
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return KnnMemoryBasedCFRS.class.isAssignableFrom(rs);
    }
}
