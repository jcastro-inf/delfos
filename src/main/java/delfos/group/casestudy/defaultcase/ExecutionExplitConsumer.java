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
package delfos.group.casestudy.defaultcase;

import delfos.common.Chronometer;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationFunction;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupRecommendationRequest;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ExecutionExplitConsumer {

    private final GroupCaseStudy groupCaseStudy;
    private final int execution;
    private final int split;
    private final PairOfTrainTestRatingsDataset[] pairsOfTrainTest;
    private final Collection<GroupOfUsers> groups;

    public ExecutionExplitConsumer(
            int execution,
            int split,
            GroupCaseStudy groupCaseStudy, PairOfTrainTestRatingsDataset[] pairsOfTrainTest,
            Collection<GroupOfUsers> groups
    ) {
        this.split = split;
        this.groupCaseStudy = (GroupCaseStudy) groupCaseStudy.clone();
        this.execution = execution;
        this.pairsOfTrainTest = pairsOfTrainTest;
        this.groups = Collections.unmodifiableCollection(groups);
    }

    public Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> execute() {
        groupCaseStudy.getLoopSeed(execution, split);

        final GroupRecommenderSystem groupRecommenderSystem = groupCaseStudy.getGroupRecommenderSystem();
        final RelevanceCriteria relevanceCriteria = groupCaseStudy.getRelevanceCriteria();
        final GroupPredictionProtocol groupPredictionProtocol = groupCaseStudy.getGroupPredictionProtocol();
        final DatasetLoader<? extends Rating> originalDatasetLoader = groupCaseStudy.getDatasetLoader();

        DatasetLoader<? extends Rating> trainDatasetLoader = pairsOfTrainTest[split].getTrainingDatasetLoader();
        trainDatasetLoader.setAlias(trainDatasetLoader.getAlias() + "_execution=" + execution);

        DatasetLoader<? extends Rating> testDatasetLoader = pairsOfTrainTest[split].getTestDatasetLoader();
        testDatasetLoader.setAlias(testDatasetLoader.getAlias() + "_execution=" + execution);

        final long recommendationModelBuildTime;
        Object groupRecommendationModel;
        {
            Chronometer buildTime = new Chronometer();
            groupRecommendationModel = groupRecommenderSystem.buildRecommendationModel(trainDatasetLoader);
            if (groupRecommendationModel == null) {
                throw new IllegalStateException("The RecommendationModel cannot be null");
            }

            long spent = buildTime.getTotalElapsed();
            recommendationModelBuildTime = spent;
        }

        List<SingleGroupRecommendationTaskInput> taskGroupRecommendationInput = new ArrayList<>(groups.size());
        for (GroupOfUsers groupOfUsers : groups) {
            for (GroupRecommendationRequest groupRecommendationRequest : groupPredictionProtocol.getGroupRecommendationRequests(trainDatasetLoader, testDatasetLoader, groupOfUsers)) {

                taskGroupRecommendationInput.add(new SingleGroupRecommendationTaskInput(
                        groupRecommenderSystem,
                        groupRecommendationRequest.predictionPhaseDatasetLoader,
                        groupRecommendationModel,
                        groupOfUsers,
                        groupRecommendationRequest.itemsToPredict)
                );
            }
        }

        taskGroupRecommendationInput.parallelStream().forEach(task -> {

            Set<Item> groupRequests = task.getItemsRequested();
            GroupOfUsers groupOfUsers = task.getGroupOfUsers();

            if (groupRequests == null) {
                throw new IllegalArgumentException("Group request for group '" + groupOfUsers.toString() + "' are null.");
            }
            if (groupRequests.isEmpty()) {
                throw new IllegalArgumentException("Group request for group '" + groupOfUsers.toString() + "' are empty.");
            }
        });

        List<SingleGroupRecommendationTaskOutput> taskGroupRecommendationOutput = taskGroupRecommendationInput
                .parallelStream()
                .map(new SingleGroupRecommendationFunction()
                )
                .collect(Collectors.toList());

        taskGroupRecommendationOutput.parallelStream().forEach(task -> {

            GroupOfUsers groupOfUsers = task.getGroup();
            final GroupRecommendations groupRecommendations = task.getRecommendations();

            if (groupRecommendations == null) {
                throw new IllegalStateException("Group recommendations for group '" + groupOfUsers.toString() + "'" + groupCaseStudy.getAlias() + " --> Cannot recommend to group a null recommendations.");
            }

            if (groupRecommendations.getRecommendations() == null) {
                throw new IllegalStateException("Group recommendations for group '" + groupOfUsers.toString() + "'" + groupCaseStudy.getAlias() + " --> Cannot recommend to group a null recommendations.");
            }

            if (groupRecommendations.getRecommendations().isEmpty()) {
                throw new IllegalStateException("Group recommendations for group '" + groupOfUsers.toString() + "'" + groupCaseStudy.getAlias() + " --> Cannot have empty recommendations.");
            }
        });

        GroupRecommenderSystemResult groupRecommendationResult
                = new GroupRecommenderSystemResult(
                        taskGroupRecommendationInput,
                        taskGroupRecommendationOutput,
                        groupCaseStudy.getAlias(),
                        execution,
                        split,
                        recommendationModelBuildTime);

        Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> resultsThisExecutionSplit = GroupEvaluationMeasuresFactory.getInstance().getAllClasses().parallelStream().collect(Collectors.toMap(Function.identity(),
                groupEvaluationMeasure -> {
                    return groupEvaluationMeasure.getMeasureResult(
                            groupRecommendationResult,
                            originalDatasetLoader,
                            testDatasetLoader.getRatingsDataset(),
                            relevanceCriteria, trainDatasetLoader, testDatasetLoader);

                }));

        return resultsThisExecutionSplit;

    }

}
