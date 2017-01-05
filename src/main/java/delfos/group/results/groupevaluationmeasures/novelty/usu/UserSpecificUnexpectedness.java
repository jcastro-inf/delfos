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
package delfos.group.results.groupevaluationmeasures.novelty.usu;

import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class UserSpecificUnexpectedness extends GroupEvaluationMeasure {

    private final int listSizeOfMeasure;

    public static class MeanByListSize {

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

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanByListSize ilsAllGroups = groupRecommenderSystemResult
                .getGroupsOfUsers().parallelStream()
                .filter(groupOfUsers -> {
                    SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput = groupRecommenderSystemResult
                            .getGroupOutput(groupOfUsers);
                    return !singleGroupRecommendationTaskOutput.getRecommendations().getRecommendations().isEmpty();
                })
                .map(groupOfUsers -> {
                    SingleGroupRecommendationTaskInput singleGroupRecommendationTaskInput = groupRecommenderSystemResult
                            .getGroupInput(groupOfUsers);

                    SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput = groupRecommenderSystemResult
                            .getGroupOutput(groupOfUsers);

                    GroupRecommendations recommendations = singleGroupRecommendationTaskOutput.getRecommendations();

                    Optional<MeanByListSize> userSpecificUnexpectedness_byMember = groupOfUsers.getMembers().stream()
                            .map((member) -> {

                                Map<Integer, ? extends Rating> memberRatings = originalDatasetLoader.getRatingsDataset()
                                        .getUserRatingsRated(member.getId());

                                MeanByListSize meanByListSize = new MeanByListSize(
                                        originalDatasetLoader,
                                        recommendations,
                                        memberRatings,
                                        listSizeOfMeasure);

                                return meanByListSize;

                            })
                            .reduce(MeanByListSize.ILS_JOINER);

                    return userSpecificUnexpectedness_byMember.get();
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

    public UserSpecificUnexpectedness() {
        this.listSizeOfMeasure = 5;
    }

    protected UserSpecificUnexpectedness(int listSizeOfMeasure) {
        this.listSizeOfMeasure = listSizeOfMeasure;

    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }

}
