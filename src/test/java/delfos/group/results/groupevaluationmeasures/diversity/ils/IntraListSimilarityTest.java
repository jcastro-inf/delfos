package delfos.group.results.groupevaluationmeasures.diversity.ils;

import delfos.common.aggregationoperators.Mean;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.util.DatasetPrinter;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationFunction;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import delfos.similaritymeasures.PearsonCorrelationCoefficient;
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

        GroupRecommenderSystem grs = new AggregationOfIndividualRatings(new KnnMemoryBasedCFRS(), new Mean());
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

    @Test
    public void testPrinPairwiseMatrix(){

        DatasetLoader<? extends Rating> ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        List<Item> items = ml100k.getContentDataset().stream().limit(10).collect(Collectors.toList());


        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();
        Map<Item,Map<Item,Double>> itemItemSimilarities = items.parallelStream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        item -> {
                            Map<Item, Double> thisItemSimilarities = items.parallelStream().collect(Collectors.toMap(Function.identity(), item2 -> {
                                Collection<CommonRating> commonRating = CommonRating.intersection(ml100k, item, item2);

                                double similarity = pcc.similarity(ml100k, item, item2);

                                similarity = (similarity + 1) / 2.0;
                                similarity = commonRating.size() >= 20? similarity: similarity* commonRating.size()/20.0;

                                return similarity;
                            }));
                            return thisItemSimilarities;
                        })
                );

        System.out.println(DatasetPrinter.printItemItem(itemItemSimilarities));
    }

    @Test
    public void testILSforUser1(){

        DatasetLoader<? extends  Rating> ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");


        Item item1 = ml100k.getContentDataset().get(8);
        Item item2 = ml100k.getContentDataset().get(1);
        Item item3 = ml100k.getContentDataset().get(5);
        Item item4 = ml100k.getContentDataset().get(6);
        Item item5 = ml100k.getContentDataset().get(10);
        Item item6 = ml100k.getContentDataset().get(2);
        Item item7 = ml100k.getContentDataset().get(7);
        Item item8 = ml100k.getContentDataset().get(4);
        Item item9 = ml100k.getContentDataset().get(3);
        Item item10 = ml100k.getContentDataset().get(9);

        Set<Item> user1Rated = Arrays.asList(item1,item2).stream().collect(Collectors.toSet());
        Set<Item> user1Recom = Arrays.asList(item3,item4,item5).stream().collect(Collectors.toSet());


        Set<Item> user2Rated = Arrays.asList(item2,item3, item4).stream().collect(Collectors.toSet());
        Set<Item> user2Recom = Arrays.asList(item1,item5).stream().collect(Collectors.toSet());

        double ils1 = IntraListSimilarity.getILS(ml100k, user1Recom);
        double ils2 = IntraListSimilarity.getILS(ml100k, user2Recom);

        System.out.println("User 1 ils: "+ils1);
        System.out.println("User 2 ils: "+ils2);
    }

}
