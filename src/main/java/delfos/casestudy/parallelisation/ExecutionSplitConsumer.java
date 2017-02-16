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
package delfos.casestudy.parallelisation;

import delfos.casestudy.defaultcase.ExecutionSplitDescriptor;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.recommendation.RecommenderSystemResult;
import delfos.rs.RecommenderSystem;
import delfos.rs.recommendation.RecommendationsToUser;
import delfos.utils.algorithm.progress.ProgressChangedController;
import delfos.utils.algorithm.progress.ProgressChangedListenerDefault;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RecommendationModel>
 * @param <RatingType>
 */
public class ExecutionSplitConsumer<RecommendationModel extends Object, RatingType extends Rating>
        implements Comparable<ExecutionSplitConsumer> {

    private final CaseStudy<RecommendationModel, RatingType> caseStudy;
    private final int execution;
    private final int split;

    public ExecutionSplitConsumer(
            int execution,
            int split,
            CaseStudy<RecommendationModel, RatingType> caseStudy
    ) {
        this.split = split;
        this.caseStudy = (CaseStudy<RecommendationModel, RatingType>) caseStudy.clone();
        this.execution = execution;

    }

    public Map<EvaluationMeasure, MeasureResult> execute() {

        final RecommenderSystem<RecommendationModel> recommenderSystem = caseStudy.getRecommenderSystem();
        final RelevanceCriteria relevanceCriteria = caseStudy.getRelevanceCriteria();
        final PredictionProtocol predictionProtocol = caseStudy.getPredictionProtocol();
        final DatasetLoader<RatingType> originalDatasetLoader = caseStudy.getDatasetLoader();

        long loopSeedForValidation = caseStudy.getLoopSeed(execution, 0);

        ValidationTechnique validationTechnique = (ValidationTechnique) caseStudy.getValidationTechnique()
                .clone();
        validationTechnique.setSeedValue(loopSeedForValidation);

        final PairOfTrainTestRatingsDataset thisExecutionSplitTrainingTestSet = validationTechnique
                .shuffle(originalDatasetLoader)[split];

        DatasetLoader<RatingType> trainDatasetLoader = thisExecutionSplitTrainingTestSet.getTrainingDatasetLoader();

        String executionString = new DecimalFormat("00").format(execution);

        trainDatasetLoader.setAlias(trainDatasetLoader.getAlias() + "_execution=" + executionString);

        DatasetLoader<RatingType> testDatasetLoader = thisExecutionSplitTrainingTestSet.getTestDatasetLoader();
        testDatasetLoader.setAlias(testDatasetLoader.getAlias() + "_execution=" + executionString);

        final long recommendationModelBuildTime;
        RecommendationModel recommendationModel;
        {
            Chronometer buildTime = new Chronometer();
            recommendationModel = recommenderSystem.buildRecommendationModel(trainDatasetLoader);
            if (recommendationModel == null) {
                throw new IllegalStateException("The RecommendationModel cannot be null");
            }

            long spent = buildTime.getTotalElapsed();
            recommendationModelBuildTime = spent;
        }

        List<RecommendationTaskInput> recommendationTaskInputs = originalDatasetLoader.getUsersDataset().parallelStream()
                .flatMap(user -> {
                    return predictionProtocol.getUserRecommendationRequests(
                            originalDatasetLoader,
                            trainDatasetLoader,
                            testDatasetLoader,
                            user).stream();
                })
                .filter(userRecommendationRequest -> !userRecommendationRequest.getItemsToPredict().isEmpty())
                .map(userRecommendationRequest -> {
                    RecommendationTaskInput singleGroupRecommendationTaskInput = new RecommendationTaskInput(
                            userRecommendationRequest.getUser(),
                            recommenderSystem,
                            userRecommendationRequest.getPredictionPhaseDatasetLoader(),
                            recommendationModel,
                            userRecommendationRequest.getItemsToPredict());

                    return singleGroupRecommendationTaskInput;
                })
                .collect(Collectors.toList());

        if (recommendationTaskInputs.isEmpty()) {
            throw new IllegalStateException("Recommendation input tasks cannot be empty");
        }

        recommendationTaskInputs.parallelStream().forEach(task -> {

            Set<Item> requests = task.getItemsRequested();
            User user = task.getUser();

            if (requests == null) {
                throw new IllegalArgumentException("Group request for group '" + user.toString() + "' are null.");
            }

            if (requests.isEmpty()) {
                Global.showWarning("User " + user.toString() + " has no requests in dataset " + task.getDatasetLoader().getAlias());
            }
        });

        final ProgressChangedController progressChangedController = new ProgressChangedController(
                "group recommendation",
                recommendationTaskInputs.size(),
                new ProgressChangedListenerDefault(System.out, 10000));

        List<RecommendationTaskOutput> recommendationTaskOutputs = recommendationTaskInputs
                .parallelStream()
                .map(new RecommendationFunction(progressChangedController))
                .collect(Collectors.toList());

        recommendationTaskOutputs.parallelStream().forEach(task -> {

            User user = task.getUser();
            RecommendationsToUser recommendationsToUser = task.getRecommendations();

            if (recommendationsToUser == null) {
                throw new IllegalStateException("Recommendations for user '" + user.toString() + "'" + caseStudy.getAlias() + " --> Recommendations object is null.");
            }

            if (recommendationsToUser.getRecommendations() == null) {
                throw new IllegalStateException("Recommendations for user '" + user.toString() + "'" + caseStudy.getAlias() + " --> Recommendation list is null.");
            }

            if (recommendationsToUser.getRecommendations().isEmpty()) {
                Global.showWarning("Recommendations for user'" + user.toString() + "'" + caseStudy.getAlias() + " --> Cannot have empty recommendations.");
            }
        });

        RecommenderSystemResult recommenderSystemResult
                = new RecommenderSystemResult(
                        recommendationTaskInputs,
                        recommendationTaskOutputs,
                        caseStudy.getAlias(),
                        execution,
                        split,
                        recommendationModelBuildTime);

        Map<EvaluationMeasure, MeasureResult> resultsThisExecutionSplit = EvaluationMeasuresFactory.getInstance().getAllClasses().parallelStream().collect(Collectors.toMap(Function.identity(),
                evaluationMeasure -> {
                    return evaluationMeasure.getMeasureResult(recommenderSystemResult,
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
        return new ExecutionSplitDescriptor(execution, split, caseStudy, this.execute());

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

    /**
     * Returns only groups with at least one rating in the test dataset.
     *
     * It is needed that at least one member has one rating to retain the group. Otherwise the group is discarded.
     *
     * @param groups
     * @param testDatasetLoader
     * @return
     */
    private Collection<GroupOfUsers> deleteGroupsWithoutValidationRatings(
            Collection<GroupOfUsers> groups,
            DatasetLoader<RatingType> testDatasetLoader) {

        final RatingsDataset<RatingType> ratingsDataset = testDatasetLoader.getRatingsDataset();

        List<GroupOfUsers> groupsWithTestRatings = groups.parallelStream()
                .filter(group -> {
                    boolean anyMatch = group.getMembers().parallelStream()
                            .anyMatch(user -> {
                                try {
                                    return ratingsDataset.isRatedUser(user.getId());
                                } catch (UserNotFound ex) {
                                    return false;
                                }
                            });

                    return anyMatch;
                })
                .collect(Collectors.toList());

        if (Global.isInfoPrinted() && groups.size() != groupsWithTestRatings.size()) {
            Global.showInfoMessage("Original groups generated: " + groups.size() + " groups with ratings: " + groupsWithTestRatings.size() + "\n");
        }

        return groupsWithTestRatings;

    }

    private Set<User> obtainUsersWithRatingsInTest(DatasetLoader<RatingType> trainDatasetLoader, DatasetLoader<RatingType> testDatasetLoader) {

        RatingsDataset<RatingType> testRatingsDataset = testDatasetLoader.getRatingsDataset();

        Set<User> usersWithRatingsInTest = trainDatasetLoader.getUsersDataset().parallelStream()
                .filter(user -> {

                    boolean ratingsInTest;
                    try {
                        ratingsInTest = testRatingsDataset.isRatedUser(user.getId());
                    } catch (UserNotFound ex) {
                        ratingsInTest = false;
                    }
                    return ratingsInTest;

                })
                .collect(Collectors.toSet());

        if (usersWithRatingsInTest.isEmpty()) {
            return obtainUsersWithRatingsInTest(trainDatasetLoader, testDatasetLoader);
        }

        return usersWithRatingsInTest;
    }
}
