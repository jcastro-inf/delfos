/*
 * Copyright (C) 2017 jcastro
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
package delfos.results.evaluationmeasures.novelty.usu;

import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class MeanByListSize {

    final List<MeanIterative> meanByListSize = Collections.synchronizedList(new ArrayList<>());

    public MeanByListSize() {
    }

    public <RatingType extends Rating> MeanByListSize(
            DatasetLoader<RatingType> datasetLoader,
            Recommendations recommendations,
            Map<Integer, ? extends Rating> memberRatings,
            int listSizeOfMeasure
    ) {

        List<Recommendation> recommendationsSorted = recommendations
                .getRecommendations().stream()
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .limit(listSizeOfMeasure)
                .collect(Collectors.toList());

        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();

        List<List<Double>> recommendationsUSU = IntStream
                .range(0, recommendations.getRecommendations().size()).boxed()
                .map(listSize -> {

                    Recommendation recommendation = recommendationsSorted.get(listSize);
                    Item item1 = recommendation.getItem();
                    return memberRatings.values().parallelStream()
                            .map(rating -> rating.getItem())
                            .map(item2 -> {
                                Collection<CommonRating> list = CommonRating
                                        .intersection(datasetLoader, item1, item2);

                                double similarity = pcc
                                        .similarity(list, datasetLoader.getRatingsDataset());
                                return similarity;
                            })
                            .map(similarity -> (similarity + 1) / 2.0)
                            .map(value -> {
                                if (Double.isNaN(value)) {
                                    return 0.0;
                                } else {
                                    return value;
                                }
                            })
                            .collect(Collectors.toList());

                }).collect(Collectors.toList());

        IntStream.range(0, recommendationsUSU.size()).boxed()
                .parallel()
                .forEach(listSize -> {
                    List<Double> usus = recommendationsUSU.subList(0, listSize + 1)
                            .parallelStream()
                            .flatMap(listForSize -> listForSize.parallelStream())
                            .map(value -> value)
                            .collect(Collectors.toList());

                    MeanIterative meanIterative = new MeanIterative(usus);
                    addILS(meanIterative.getMean(), listSize);

                });

    }

    public void addILS(double value, int listSize) {

        synchronized (meanByListSize) {
            while (meanByListSize.size() < listSize) {
                meanByListSize.add(new MeanIterative());
            }

            int index = listSize - 1;

            meanByListSize.get(index).addValue(value);
        }
    }

    public double getILS(int listSize) {
        int index = listSize - 1;
        return meanByListSize.get(index).getMean();
    }

    private MeanIterative getMeanILS(int listSize) {
        int index = listSize - 1;
        return meanByListSize.get(index);
    }

    private boolean contains(int listSize) {
        if (this.meanByListSize.isEmpty()) {
            return false;
        }

        int sizeShouldHave = listSize;

        return (sizeShouldHave <= this.meanByListSize.size());
    }

    int size() {
        return this.meanByListSize.size();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("MIUF\n");

        for (int listSize = 1; listSize <= this.meanByListSize.size(); listSize++) {
            str
                    .append("lenght= ").append(listSize).append(" \t\t")
                    .append("MIUF= ").append(getILS(listSize)).append("\n");
        }

        return str.toString();
    }

    public static final BinaryOperator<MeanByListSize> ILS_JOINER = (MeanByListSize t, MeanByListSize u) -> {
        MeanByListSize ret = new MeanByListSize();

        int maxSize = Math.max(t.meanByListSize.size(), u.meanByListSize.size());

        while (ret.meanByListSize.size() < maxSize) {
            ret.meanByListSize.add(new MeanIterative());
        }

        for (int listSize = 1; listSize <= maxSize; listSize++) {

            if (t.contains(listSize)) {
                MeanIterative meanIterativeA = t.getMeanILS(listSize);
                ret.getMeanILS(listSize).addMean(meanIterativeA);
            }

            if (u.contains(listSize)) {
                MeanIterative meanIterativeB = u.getMeanILS(listSize);
                ret.getMeanILS(listSize).addMean(meanIterativeB);
            }
        }
        return ret;
    };
}
