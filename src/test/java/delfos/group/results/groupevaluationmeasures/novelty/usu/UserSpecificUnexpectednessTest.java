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
package delfos.group.results.groupevaluationmeasures.novelty.usu;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.groupevaluationmeasures.MAETest;
import delfos.group.results.groupevaluationmeasures.RatingsDatasetMock;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;

import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class UserSpecificUnexpectednessTest extends DelfosTest {

    public UserSpecificUnexpectednessTest() {
    }

    public void test() {
        //Phase 1: Preparation
        final RatingsDataset<Rating> testDataset = new RatingsDatasetMock();

        DatasetLoader<? extends Rating> datasetLoader = new CompleteDatasetLoaderAbstract<Rating>() {

            @Override
            public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
                return testDataset;
            }
        };

        Object recommendationModel = null;

        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(4);

        GroupOfUsers groupOfUsers = new GroupOfUsers(1l, 2l);
        Set<Item> groupRequests = Arrays.asList(new Item(12), new Item(13)).stream().collect(Collectors.toSet());

        List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs = Arrays.asList(
                new SingleGroupRecommendationTaskInput(null, datasetLoader, recommendationModel, groupOfUsers, groupRequests));

        GroupRecommendations groupRecommendations = new GroupRecommendations(groupOfUsers, Collections.EMPTY_LIST);
        long buildTime = 0;
        long groupBuildTime = 0;
        long groupRecommendationTime = 0;
        List<SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs = Arrays.asList(
                new SingleGroupRecommendationTaskOutput(groupOfUsers, groupRecommendations, buildTime, groupRecommendationTime));

        GroupRecommenderSystemResult groupRecommenderSystemResult = new GroupRecommenderSystemResult(
                singleGroupRecommendationInputs,
                singleGroupRecommendationOutputs,
                MAETest.class.getSimpleName(),
                0,
                0,
                -1);

        UserSpecificUnexpectedness instance = new UserSpecificUnexpectedness();

        //Phase 2: Execution
        GroupEvaluationMeasureResult groupMaeResult = instance.getMeasureResult(groupRecommenderSystemResult,
                datasetLoader,
                relevanceCriteria,
                datasetLoader,
                datasetLoader);

        //Phase 3: Result checking
        double expResult = Double.NaN;
        double delta = 0.001f;
        assertEquals(expResult, groupMaeResult.getValue(), delta);

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
    public void testUSUforUser1(){

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

        double usu1 = UserSpecificUnexpectedness.getUSU(ml100k,user1Rated, user1Recom);
        double usu2 = UserSpecificUnexpectedness.getUSU(ml100k,user2Rated, user2Recom);

        System.out.println("User 1 usu: "+usu1);
        System.out.println("User 2 usu: "+usu2);
    }
}
