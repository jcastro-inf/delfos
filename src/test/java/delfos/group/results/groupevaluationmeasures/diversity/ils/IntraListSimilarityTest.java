package delfos.group.results.groupevaluationmeasures.diversity.ils;

import delfos.common.aggregationoperators.Mean;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationFunction;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class IntraListSimilarityTest {

    public IntraListSimilarityTest() {
    }

    /**
     * Test of getMeasureResultForSingleGroup method, of class IntraListSimilarity.
     */
    @Test
    public void testGetMeasureResultForSingleGroup() {
        System.out.println("getMeasureResultForSingleGroup");

        DatasetLoader<? extends Rating> originalDatasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        Set<User> groupMembers = Arrays.asList(1, 2, 3, 4, 5).stream().map(idUser -> originalDatasetLoader.getUsersDataset().getUser(idUser)).collect(Collectors.toSet());
        GroupOfUsers groupOfUsers = new GroupOfUsers(groupMembers);

        CrossFoldValidation_Ratings crossFoldValidation = new CrossFoldValidation_Ratings();
        crossFoldValidation.setSeedValue(123456);

        PairOfTrainTestRatingsDataset[] shuffle = crossFoldValidation
                .shuffle(originalDatasetLoader);

        DatasetLoader<? extends Rating> trainingDatasetLoader = shuffle[0].getTrainingDatasetLoader();
        DatasetLoader<? extends Rating> testDatasetLoader = shuffle[0].getTestDatasetLoader();
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria();

        GroupRecommenderSystem grs = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean());
        Object buildRecommendationModel = grs.buildRecommendationModel(originalDatasetLoader);

        Set<Item> candidateItems = new OnlyNewItems().candidateItems(originalDatasetLoader, groupOfUsers);

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
        IntraListSimilarity.IntraListSimilarityByRecommendationLenght resultOfGroup = instance.getMeasureResultForSingleGroup(
                groupOfUsers,
                singleGroupRecommendationTaskInput,
                singleGroupRecommendationTaskOutput,
                originalDatasetLoader,
                relevanceCriteria,
                trainingDatasetLoader,
                testDatasetLoader);

        System.out.println("ILS for group " + groupOfUsers);
        for (int listSize = 1; listSize <= resultOfGroup.size(); listSize++) {
            System.out.println("lenght= " + listSize + " \t\tILS= " + resultOfGroup.getILS(listSize));
        }

        System.out.println("\n\n\n");
    }

}
