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
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.groupevaluationmeasures.MAETest;
import delfos.group.results.groupevaluationmeasures.RatingsDatasetMock;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class UserSpecificUnexpectednessTest extends DelfosTest {

    public UserSpecificUnexpectednessTest() {
    }

    @Test
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

        GroupOfUsers groupOfUsers = new GroupOfUsers(1, 2);
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
}
