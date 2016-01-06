package delfos.group.results.groupevaluationmeasures;

import delfos.Constants;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CosineCoefficient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jdom2.Element;

/**
 * Evaluation metric that computes the similarity of the items recommended. This
 * measure is intended to capture the diversity of the recommender. A lower
 * value means that the recommender generates more diverse recommendations.
 *
 * @author Jorge Castro Gallardo
 *
 */
public class IntraListSimilarity extends GroupEvaluationMeasure {

    class IntraListSimilarityByRecommendationLenght {

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

        public IntraListSimilarityByRecommendationLenght join(IntraListSimilarityByRecommendationLenght a, IntraListSimilarityByRecommendationLenght b) {
            IntraListSimilarityByRecommendationLenght ret = new IntraListSimilarityByRecommendationLenght();

            int maxSize = Math.max(a.meanByListSize.size(), b.meanByListSize.size());

            while (ret.meanByListSize.size() < maxSize) {
                ret.meanByListSize.add(new MeanIterative());
            }

            for (int listSize = 1; listSize <= maxSize; listSize++) {

                if (a.contains(listSize)) {
                    MeanIterative meanIterativeA = a.getMeanILS(listSize);
                    ret.getMeanILS(listSize).addMean(meanIterativeA);
                }

                if (b.contains(listSize)) {
                    MeanIterative meanIterativeB = b.getMeanILS(listSize);
                    ret.getMeanILS(listSize).addMean(meanIterativeB);
                }
            }
            return ret;
        }

        private boolean contains(int listSize) {
            int sizeShouldHave = listSize;

            return (this.meanByListSize.size() <= sizeShouldHave);
        }

        int size() {
            return this.meanByListSize.size();
        }
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative meanIterative = new MeanIterative();
        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult) {

            SingleGroupRecommendationTaskInput singleGroupRecommendationTaskInput = groupRecommenderSystemResult.getGroupInput(groupOfUsers);;
            SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput = groupRecommenderSystemResult.getGroupOutput(groupOfUsers);

            GroupEvaluationMeasureResult thisGroupResult = getMeasureResultForSingleGroup(
                    groupOfUsers,
                    singleGroupRecommendationTaskInput,
                    singleGroupRecommendationTaskOutput,
                    originalDatasetLoader,
                    testDataset,
                    relevanceCriteria,
                    trainingDatasetLoader,
                    testDatasetLoader);

            meanIterative.addValue(thisGroupResult.getValue());
        }

        return new GroupEvaluationMeasureResult(this, meanIterative.getMean());

    }

    public GroupEvaluationMeasureResult getMeasureResultForSingleGroup(
            GroupOfUsers groupOfUsers,
            SingleGroupRecommendationTaskInput singleGroupRecommendationTaskInput,
            SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {
        IntraListSimilarityByRecommendationLenght result = new IntraListSimilarityByRecommendationLenght();

        TryThisAtHomeSVDModel svdModel = getSVDModel(originalDatasetLoader);

        List<Recommendation> recommendations = singleGroupRecommendationTaskOutput.getRecommendations().stream().sorted(Recommendation.BY_PREFERENCE_DESC).collect(Collectors.toList());

        recommendations = recommendations.subList(0, Math.min(50, recommendations.size()));

        for (int listSize = 1; listSize <= recommendations.size(); listSize++) {

            List<Recommendation> recommendationSubList = recommendations.subList(0, listSize);

            double intraListSimilarity = intraListSimilarity(svdModel, recommendationSubList);

            result.addILS(intraListSimilarity, listSize);
        }

        double measureValue;
        if (result.size() >= 5) {
            measureValue = result.getILS(5);
        } else {
            measureValue = result.getILS(result.size());
        }

        return new GroupEvaluationMeasureResult(this, measureValue, new Element(this.getClass().getSimpleName()), result);
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

                List<Float> item1Features = svdModel.getItemFeatures(item1.getId()).stream().map(value -> ((Number) value).floatValue()).collect(Collectors.toList());
                List<Float> item2Features = svdModel.getItemFeatures(item2.getId()).stream().map(value -> ((Number) value).floatValue()).collect(Collectors.toList());

                float similarity = cosineCoefficient.similarity(item1Features, item2Features);

                if (Float.isFinite(similarity)) {
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

    public TryThisAtHomeSVDModel getSVDModel(DatasetLoader<? extends Rating> originalDatasetLoader) throws CannotLoadContentDataset, CannotLoadRatingsDataset {
        TryThisAtHomeSVD svd = new TryThisAtHomeSVD(20, 20);
        svd.setParameterValue(TryThisAtHomeSVD.NORMALIZE_WITH_USER_MEAN, true);

        RecommenderSystem_fixedFilePersistence rs_persistence = new RecommenderSystem_fixedFilePersistence(svd,
                new FilePersistence(originalDatasetLoader.getAlias() + "_numFeatures=20_numIter=20_normalised", "svd.model", Constants.getTempDirectory()));

        TryThisAtHomeSVDModel svdModel = (TryThisAtHomeSVDModel) rs_persistence.buildRecommendationModel(originalDatasetLoader);
        return svdModel;
    }
}
