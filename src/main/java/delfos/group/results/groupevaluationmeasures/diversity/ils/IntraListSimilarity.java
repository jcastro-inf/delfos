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
package delfos.group.results.groupevaluationmeasures.diversity.ils;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.bufferedrecommenders.RecommenderSystem_cacheRecommendationModel;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CosineCoefficient;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import org.jdom2.Element;

/**
 * Evaluation metric that computes the similarity of the items recommended. This measure is intended to capture the
 * diversity of the recommender. A lower value means that the recommender generates more diverse recommendations.
 *
 * Measures the Average Intra-List Distance (Recommender Systems Handbook, 2nd edition, 26.3.2).
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class IntraListSimilarity extends GroupEvaluationMeasure {

    protected final int listSizeOfMeasure;
    protected final int maxlistSizeOfMeasure = 50;

    public IntraListSimilarity() {
        listSizeOfMeasure = 5;
    }

    protected IntraListSimilarity(int listSizeOfMeasure) {
        this.listSizeOfMeasure = listSizeOfMeasure;
    }

    public static final BinaryOperator<IntraListSimilarityByRecommendationLenght> ILS_JOINER = new BinaryOperator<IntraListSimilarityByRecommendationLenght>() {

        @Override
        public IntraListSimilarityByRecommendationLenght apply(IntraListSimilarityByRecommendationLenght t, IntraListSimilarityByRecommendationLenght u) {
            IntraListSimilarityByRecommendationLenght ret = new IntraListSimilarityByRecommendationLenght();

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
        }
    };

    protected static class IntraListSimilarityByRecommendationLenght {

        List<MeanIterative> meanByListSize;

        public IntraListSimilarityByRecommendationLenght() {

            meanByListSize = new ArrayList<>();
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
            StringBuilder str = new StringBuilder("ILS\n");

            for (int listSize = 1; listSize <= this.meanByListSize.size(); listSize++) {
                str.append("lenght= ").append(listSize);
                str.append(" \t\tILS= ").append(getILS(listSize));
                str.append("\n");
            }

            return str.toString();
        }

    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult, DatasetLoader<? extends Rating> originalDatasetLoader, RelevanceCriteria relevanceCriteria, DatasetLoader<? extends Rating> trainingDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader) {

        IntraListSimilarityByRecommendationLenght ilsAllGroups = groupRecommenderSystemResult
                .getGroupsOfUsers().parallelStream()
                .filter(groupOfUsers -> {
                    SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput = groupRecommenderSystemResult.getGroupOutput(groupOfUsers);
                    return !singleGroupRecommendationTaskOutput.getRecommendations().getRecommendations().isEmpty();
                })
                .map(groupOfUsers -> {

                    SingleGroupRecommendationTaskInput singleGroupRecommendationTaskInput = groupRecommenderSystemResult.getGroupInput(groupOfUsers);
                    SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput = groupRecommenderSystemResult.getGroupOutput(groupOfUsers);

                    IntraListSimilarityByRecommendationLenght intraListSimilarityByRecommendationLenght
                            = getMeasureResultForSingleGroup(
                                    groupOfUsers,
                                    singleGroupRecommendationTaskInput,
                                    singleGroupRecommendationTaskOutput,
                                    originalDatasetLoader,
                                    relevanceCriteria,
                                    trainingDatasetLoader,
                                    testDatasetLoader);

                    return intraListSimilarityByRecommendationLenght;
                })
                .reduce(ILS_JOINER)
                .get();

        double measureValue;
        if (ilsAllGroups.size() >= this.listSizeOfMeasure) {
            measureValue = ilsAllGroups.getILS(this.listSizeOfMeasure);
        } else {
            measureValue = ilsAllGroups.getILS(ilsAllGroups.size());
        }

        return new GroupEvaluationMeasureResult(this, measureValue);

    }

    public IntraListSimilarityByRecommendationLenght getMeasureResultForSingleGroup(
            GroupOfUsers groupOfUsers,
            SingleGroupRecommendationTaskInput singleGroupRecommendationTaskInput,
            SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {
        IntraListSimilarityByRecommendationLenght ilsThisGroup = new IntraListSimilarityByRecommendationLenght();

        TryThisAtHomeSVDModel svdModel = getSVDModel(originalDatasetLoader);

        List<Recommendation> recommendations = singleGroupRecommendationTaskOutput.getRecommendations().getRecommendations().stream().sorted(Recommendation.BY_PREFERENCE_DESC).collect(Collectors.toList());

        recommendations = recommendations.subList(0, Math.min(maxlistSizeOfMeasure, recommendations.size()));

        for (int listSize = 1; listSize <= recommendations.size(); listSize++) {

            List<Recommendation> recommendationSubList = recommendations.subList(0, listSize);

            double intraListSimilarity = intraListSimilarity(svdModel, recommendationSubList);

            ilsThisGroup.addILS(intraListSimilarity, listSize);
        }

        double measureValue;
        if (ilsThisGroup.size() >= this.listSizeOfMeasure) {
            measureValue = ilsThisGroup.getILS(this.listSizeOfMeasure);
        } else {
            measureValue = ilsThisGroup.getILS(ilsThisGroup.size());
        }

        return ilsThisGroup;
    }

    private double intraListSimilarity(TryThisAtHomeSVDModel svdModel, List<Recommendation> recommendations) {
        double sumOfSimilarities = 0;
        long numSimilarities = 0;
        CosineCoefficient cosineCoefficient = new CosineCoefficient();

        for (Recommendation recommendation1 : recommendations) {
            for (Recommendation recommendation2 : recommendations) {
                final Item item1 = recommendation1.getItem();
                final Item item2 = recommendation2.getItem();

                if (item1.getId() <= item2.getId()) {
                    continue;
                }

                List<Double> item1Features = svdModel.getItemFeatures(item1.getId()).stream().map(value -> ((Number) value).doubleValue()).collect(Collectors.toList());
                List<Double> item2Features = svdModel.getItemFeatures(item2.getId()).stream().map(value -> ((Number) value).doubleValue()).collect(Collectors.toList());

                double similarity = cosineCoefficient.similarity(item1Features, item2Features);

                if (Double.isFinite(similarity)) {
                    sumOfSimilarities += similarity;
                } else {
                    sumOfSimilarities += 1;
                }
                numSimilarities++;
            }
        }

        if (numSimilarities == 0) {
            return 0;
        } else {
            return sumOfSimilarities / numSimilarities;
        }
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    private static RecommenderSystem cacheSVD = null;
    private static final Object generalExMut = 1;

    public static TryThisAtHomeSVDModel getSVDModel(DatasetLoader<? extends Rating> originalDatasetLoader) throws CannotLoadContentDataset, CannotLoadRatingsDataset {

        synchronized (generalExMut) {
            if (cacheSVD == null) {
                TryThisAtHomeSVD svd = new TryThisAtHomeSVD(20, 20).setNormalizeWithUserMean(true);
                svd.setParameterValue(TryThisAtHomeSVD.PREDICT_IN_RATING_RANGE, true);
                svd.setSeedValue(123456);

                File directory = new File(RecommenderSystem_cacheRecommendationModel.DEFAULT_DIRECTORY.getAbsolutePath() + File.separator + "ILS_models" + File.separator);
                cacheSVD = new RecommenderSystem_cacheRecommendationModel()
                        .setRecommenderSystem(svd).setDirectory(directory);
                cacheSVD.addRecommendationModelBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 5000));
            }

            TryThisAtHomeSVDModel svdModel = (TryThisAtHomeSVDModel) cacheSVD.buildRecommendationModel(originalDatasetLoader);

            return svdModel;
        }

    }

    private static synchronized Element getXMLElement(IntraListSimilarity intraListSimilarity, IntraListSimilarityByRecommendationLenght intraListSimilarityByRecommendationLenght) {

        double measureValue;
        if (intraListSimilarityByRecommendationLenght.size() >= intraListSimilarity.listSizeOfMeasure) {
            measureValue = intraListSimilarityByRecommendationLenght.getILS(intraListSimilarity.listSizeOfMeasure);
        } else {
            measureValue = intraListSimilarityByRecommendationLenght.getILS(intraListSimilarityByRecommendationLenght.size());
        }

        Element ilsXMLElement = new Element(intraListSimilarity.getClass().getSimpleName());
        ilsXMLElement.setAttribute(GroupEvaluationMeasure.VALUE, Double.toString(measureValue));

        Element detailedElement = new Element("ILSdetailed");
        for (int listSize = 1; listSize <= intraListSimilarityByRecommendationLenght.size(); listSize++) {
            double intraListSimilarityValue = intraListSimilarityByRecommendationLenght.getILS(listSize);

            Element thisListSizeElement = new Element("Size");
            thisListSizeElement.setAttribute("k", Integer.toString(listSize));
            thisListSizeElement.setAttribute("ils", Double.toString(intraListSimilarityValue));
            detailedElement.addContent(thisListSizeElement);
        }

        ilsXMLElement.setContent(detailedElement);

        return ilsXMLElement;

    }

    public static double getILS(DatasetLoader<? extends  Rating> datasetLoader,Set<Item> recommendations) {
        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();

        List<Double> similarities = recommendations.stream().flatMapToDouble(item1 -> {
            DoubleStream thisItemSimilarities = recommendations.stream()
                    .filter(item2 -> item1.getId() < item2.getId())
                    .mapToDouble(item2 -> {
                        Collection<CommonRating> commonRating = CommonRating.intersection(datasetLoader, item1, item2);

                        double similarity = pcc.similarity(datasetLoader, item1, item2);
                        similarity = (similarity + 1) / 2.0;
                        similarity = commonRating.size() >= 20 ? similarity : similarity * commonRating.size() / 20.0;
                        return similarity;
                    }).filter(value -> !Double.isNaN(value));
            return thisItemSimilarities;
        }).boxed().collect(Collectors.toList());

        double ils = similarities.stream().mapToDouble(d-> d).average().getAsDouble();
        return ils;
    }
}
