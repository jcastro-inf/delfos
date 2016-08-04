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
import delfos.common.Global;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationFunction;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.GroupFormationTechniqueProgressListener_default;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupRecommendationRequest;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ExecutionSplitConsumer implements Comparable<ExecutionSplitConsumer> {

    private final GroupCaseStudy groupCaseStudy;
    private final int execution;
    private final int split;

    public ExecutionSplitConsumer(
            int execution,
            int split,
            GroupCaseStudy groupCaseStudy
    ) {
        this.split = split;
        this.groupCaseStudy = (GroupCaseStudy) groupCaseStudy.clone();
        this.execution = execution;

    }

    public Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> execute() {

        final GroupRecommenderSystem groupRecommenderSystem = groupCaseStudy.getGroupRecommenderSystem();
        final RelevanceCriteria relevanceCriteria = groupCaseStudy.getRelevanceCriteria();
        final GroupPredictionProtocol groupPredictionProtocol = groupCaseStudy.getGroupPredictionProtocol();
        final DatasetLoader<? extends Rating> originalDatasetLoader = groupCaseStudy.getDatasetLoader();

        long loopSeed = groupCaseStudy.getLoopSeed(execution, split);

        long loopSeedForValidation = groupCaseStudy.getLoopSeed(execution, 0);

        ValidationTechnique validationTechnique = (ValidationTechnique) groupCaseStudy.getValidationTechnique()
                .clone();
        validationTechnique.setSeedValue(loopSeedForValidation);

        final PairOfTrainTestRatingsDataset thisExecutionSplitTrainingTestSet = validationTechnique
                .shuffle(originalDatasetLoader)[split];

        DatasetLoader<? extends Rating> trainDatasetLoader = thisExecutionSplitTrainingTestSet.getTrainingDatasetLoader();

        String executionString = new DecimalFormat("00").format(execution);

        trainDatasetLoader.setAlias(trainDatasetLoader.getAlias() + "_execution=" + executionString);

        DatasetLoader<? extends Rating> testDatasetLoader = thisExecutionSplitTrainingTestSet.getTestDatasetLoader();
        testDatasetLoader.setAlias(testDatasetLoader.getAlias() + "_execution=" + executionString);

        final GroupFormationTechnique groupFormationTechnique = (GroupFormationTechnique) groupCaseStudy.getGroupFormationTechnique().clone();

        groupFormationTechnique.setSeedValue(loopSeed);

        if (Global.isInfoPrinted()) {
            groupFormationTechnique.addListener(new GroupFormationTechniqueProgressListener_default(System.out, 300000));
        }

        Collection<GroupOfUsers> groups = groupFormationTechnique.generateGroups(trainDatasetLoader);
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
            final Collection<GroupRecommendationRequest> groupRecommendationRequests = groupPredictionProtocol.getGroupRecommendationRequests(trainDatasetLoader, testDatasetLoader, groupOfUsers);
            for (GroupRecommendationRequest groupRecommendationRequest : groupRecommendationRequests) {

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
                Global.showWarning("Group " + groupOfUsers.toString() + " has no requests in dataset " + task.getDatasetLoader().getAlias());
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
                Global.showWarning("Group recommendations for group '" + groupOfUsers.toString() + "'" + groupCaseStudy.getAlias() + " --> Cannot have empty recommendations.");
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
                    return groupEvaluationMeasure.getMeasureResult(groupRecommendationResult,
                            originalDatasetLoader,
                            relevanceCriteria, trainDatasetLoader, testDatasetLoader);

                }));

        return resultsThisExecutionSplit;

    }

    public int getExecution() {
        return execution;
    }

    public int getSplit() {
        return split;
    }

    public ExecutionSplitDescriptor getDescriptorAndResults() {
        return new ExecutionSplitDescriptor(execution, split, groupCaseStudy, this.execute());

    }

    @Override
    public int compareTo(ExecutionSplitConsumer o) {

        int thisExecution = this.getExecution();
        int otherExecution = o.getExecution();

        int executionCompare = Integer.compare(thisExecution, otherExecution);

        if (executionCompare != 0) {
            return executionCompare;
        } else {

            int thisSplit = this.getSplit();
            int otherSplit = o.getSplit();
            int splitCompare = Integer.compare(thisSplit, otherSplit);
            return splitCompare;

        }
    }
}
