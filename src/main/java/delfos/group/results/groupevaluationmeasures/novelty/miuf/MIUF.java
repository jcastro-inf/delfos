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
package delfos.group.results.groupevaluationmeasures.novelty.miuf;

import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Measures the Global Long-Tail Novelty (see Recommender Systems Handbook, 26.3.3). It is calculated using the
 * probability of items being known by users.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class MIUF extends GroupEvaluationMeasure {

    private final int listSizeOfMeasure;

    public static final Map<Long, Double> getIUF_byItem(DatasetLoader<? extends Rating> datasetLoader) {

        return datasetLoader.getContentDataset()
                .stream().parallel()
                .collect(Collectors.toMap(item -> item.getId(), item -> {

                    double numUsersRated = datasetLoader.getRatingsDataset().getItemRated(item.getId()).size();
                    double numUsers = datasetLoader.getUsersDataset().size();

                    double iuf = -Math.log(numUsersRated / numUsers);

                    return iuf;
                }));
    }

    public static class MeanByListSize {

        List<MeanIterative> meanByListSize;

        public MeanByListSize() {
            meanByListSize = new ArrayList<>();
        }

        public MeanByListSize(Recommendations recommendations, Map<Long, Double> iuf_byItem) {
            List<Double> recommendationsIUF = recommendations.getRecommendations().stream()
                    .map(recommendation -> iuf_byItem.get(recommendation.getItem().getId()))
                    .collect(Collectors.toList());

            meanByListSize = IntStream.range(0, recommendations.getRecommendations().size()).boxed()
                    .map(listSize -> {
                        MeanIterative meanUpToSize = new MeanIterative(recommendationsIUF.subList(0, listSize + 1));
                        return new MeanIterative(Arrays.asList(meanUpToSize.getMean()));
                    }).collect(Collectors.toList());

        }

        public void addILS(double value, int listSize) {

            while (meanByListSize.size() < listSize) {
                meanByListSize.add(new MeanIterative());
            }

            int index = listSize - 1;

            meanByListSize.get(index).addValue(value);
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

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        Map<Long, Double> iuf_byItem = getIUF_byItem(originalDatasetLoader);

        MeanByListSize ilsAllGroups = groupRecommenderSystemResult
                .getGroupsOfUsers().parallelStream()
                .filter(groupOfUsers -> {
                    SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput = groupRecommenderSystemResult.getGroupOutput(groupOfUsers);
                    return !singleGroupRecommendationTaskOutput.getRecommendations().getRecommendations().isEmpty();
                })
                .map(groupOfUsers -> {

                    SingleGroupRecommendationTaskInput singleGroupRecommendationTaskInput = groupRecommenderSystemResult.getGroupInput(groupOfUsers);
                    SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput = groupRecommenderSystemResult.getGroupOutput(groupOfUsers);

                    MeanByListSize meanByListSize = new MeanByListSize(singleGroupRecommendationTaskOutput.getRecommendations(), iuf_byItem);

                    return meanByListSize;
                })
                .reduce(MeanByListSize.ILS_JOINER)
                .get();

        double measureValue;
        if (ilsAllGroups.size() >= this.listSizeOfMeasure) {
            measureValue = ilsAllGroups.getILS(this.listSizeOfMeasure);
        } else {
            measureValue = ilsAllGroups.getILS(ilsAllGroups.size());
        }

        return new GroupEvaluationMeasureResult(this, measureValue);
    }

    public MIUF() {
        this.listSizeOfMeasure = 5;
    }

    protected MIUF(int listSizeOfMeasure) {
        this.listSizeOfMeasure = listSizeOfMeasure;

    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }

}
