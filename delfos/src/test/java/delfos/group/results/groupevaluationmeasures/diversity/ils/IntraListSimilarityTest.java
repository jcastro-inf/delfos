package delfos.group.results.groupevaluationmeasures.diversity.ils;

import delfos.common.aggregationoperators.Mean;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationFunction;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_groupRatedItems;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class IntraListSimilarityTest {

    public IntraListSimilarityTest() {
    }

    /**
     * Test of getMeasureResult method, of class IntraListSimilarity.
     */
    public void testGetMeasureResult() {
        System.out.println("getMeasureResult");
        GroupRecommenderSystemResult groupRecommenderSystemResult = null;
        DatasetLoader<? extends Rating> originalDatasetLoader = null;
        RatingsDataset<? extends Rating> testDataset = null;
        RelevanceCriteria relevanceCriteria = null;
        DatasetLoader<? extends Rating> trainingDatasetLoader = null;
        DatasetLoader<? extends Rating> testDatasetLoader = null;
        IntraListSimilarity instance = new IntraListSimilarity();
        GroupEvaluationMeasureResult expResult = null;
        GroupEvaluationMeasureResult result = instance.getMeasureResult(groupRecommenderSystemResult, originalDatasetLoader, testDataset, relevanceCriteria, trainingDatasetLoader, testDatasetLoader);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMeasureResultForSingleGroup method, of class
     * IntraListSimilarity.
     */
    @Test
    public void testGetMeasureResultForSingleGroup() {
        System.out.println("getMeasureResultForSingleGroup");

        DatasetLoader<? extends Rating> originalDatasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        Set<User> groupMembers = Arrays.asList(1, 2, 3, 4, 5).stream().map(idUser -> originalDatasetLoader.getUsersDataset().getUser(idUser)).collect(Collectors.toSet());
        GroupOfUsers groupOfUsers = new GroupOfUsers(groupMembers);

        CrossFoldValidation_groupRatedItems crossFoldValidation = new CrossFoldValidation_groupRatedItems();
        crossFoldValidation.setSeedValue(123456);

        PairOfTrainTestRatingsDataset[] shuffle = crossFoldValidation
                .shuffle(originalDatasetLoader, groupOfUsers);

        DatasetLoader<? extends Rating> trainingDatasetLoader = shuffle[0].getTrainingDatasetLoader();
        DatasetLoader<? extends Rating> testDatasetLoader = shuffle[0].getTestDatasetLoader();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria();

        GroupRecommenderSystem grs = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean());
        Object buildRecommendationModel = grs.buildRecommendationModel(originalDatasetLoader);

        Set<Integer> candidateItems = new OnlyNewItems().candidateItems(originalDatasetLoader, groupOfUsers);

        SingleGroupRecommendationTaskInput singleGroupRecommendationTaskInput
                = new SingleGroupRecommendationTaskInput(
                        grs,
                        trainingDatasetLoader,
                        buildRecommendationModel,
                        groupOfUsers,
                        candidateItems);

        SingleGroupRecommendationTaskOutput singleGroupRecommendationTaskOutput
                = new SingleGroupRecommendationFunction()
                .apply(singleGroupRecommendationTaskInput);

        IntraListSimilarity instance = new IntraListSimilarity();
        GroupEvaluationMeasureResult result = instance.getMeasureResultForSingleGroup(
                groupOfUsers,
                singleGroupRecommendationTaskInput,
                singleGroupRecommendationTaskOutput,
                originalDatasetLoader,
                testDatasetLoader.getRatingsDataset(),
                relevanceCriteria,
                trainingDatasetLoader,
                testDatasetLoader);

        IntraListSimilarity.IntraListSimilarityByRecommendationLenght resultOfGroup = (IntraListSimilarity.IntraListSimilarityByRecommendationLenght) result.getDetailedResult();

        System.out.println("ILS for group " + groupOfUsers);
        for (int listSize = 1; listSize <= resultOfGroup.size(); listSize++) {
            System.out.println("lenght= " + listSize + " \t\tILS= " + resultOfGroup.getILS(listSize));
        }

        System.out.println("\n\n\n");
    }

}
