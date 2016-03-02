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
package delfos.group.grouplevelcasestudy;

import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupRecommendationRequest;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Realiza un caso de estudio a nivel de grupo, es decir, aplica las medidas de
 * evaluación sólo por cada grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-Jun-2013
 */
public class GroupLevelCaseStudy {

    public GroupLevelCaseStudy() {
    }

    public void execute(
            DatasetLoader<? extends Rating> originalDatasetLoader,
            GroupFormationTechnique groupFormation,
            GroupRecommenderSystem[] groupRecommenderSystems,
            GroupValidationTechnique validationTechnique,
            GroupPredictionProtocol predictionProtocol,
            GroupMeasure[] grouMeasures,
            Collection<GroupEvaluationMeasure> evaluationMeasures)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, ItemNotFound, NotEnoughtUserInformation {

        groupFormation.shuffle(originalDatasetLoader);
        Collection<GroupOfUsers> groups = groupFormation.shuffle(originalDatasetLoader);

        final RelevanceCriteria relevanceCriteria = originalDatasetLoader.getDefaultRelevanceCriteria();

        int numSplit = validationTechnique.getNumberOfSplits();
        Map<GroupOfUsers, Map<Integer, GroupLevelResults>> allResultsCaseStudy = new TreeMap<>();

        Global.showln("");
        Global.showln("===============================================================");
        Global.showln("===================== RESULTADOS POR GRUPO ====================");
        Global.showln("===============================================================");
        Global.showln("");

        {
            //Línea de cabecera.
            StringBuilder line = new StringBuilder();
            line.append("group\tsplit");
            for (GroupMeasure groupMeasure : grouMeasures) {
                line.append("\t").append(groupMeasure.getAlias());
            }

            for (GroupEvaluationMeasure groupEvaluationMeasure : evaluationMeasures) {
                for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {
                    line.append("\t").append(groupRecommenderSystem.getAlias()).append("-->").append(groupEvaluationMeasure.getAlias());
                }
            }
            Global.showln(line.toString());
        }

        int i = 0;
        for (GroupOfUsers group : groups) {
            Chronometer c = new Chronometer();

            PairOfTrainTestRatingsDataset[] pairs = validationTechnique.shuffle(originalDatasetLoader, groups);

            allResultsCaseStudy.put(group, new TreeMap<>());

            for (int split = 0; split < numSplit; split++) {

                GroupLevelResults groupLevelResults = new GroupLevelResults(group);
                DatasetLoader<? extends Rating> trainingDatasetLoader = pairs[split].getTrainingDatasetLoader();
                DatasetLoader<? extends Rating> testDatasetLoader = pairs[split].getTestDatasetLoader();

                for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {
                    Object recommendationModel = groupRecommenderSystem.buildRecommendationModel(trainingDatasetLoader);
                    Collection<Recommendation> allPredictions = new ArrayList<>();
                    Set<Item> requests = new TreeSet<>();

                    for (GroupRecommendationRequest groupRecommendationRequest : predictionProtocol.getGroupRecommendationRequests(trainingDatasetLoader, testDatasetLoader, group)) {

                        Object groupModel = groupRecommenderSystem.buildGroupModel(
                                groupRecommendationRequest.predictionPhaseDatasetLoader,
                                recommendationModel,
                                group);

                        Collection<Recommendation> groupRecommendations = groupRecommenderSystem.recommendOnly(
                                groupRecommendationRequest.predictionPhaseDatasetLoader,
                                recommendationModel,
                                groupModel,
                                group,
                                groupRecommendationRequest.itemsToPredict);

                        allPredictions.addAll(groupRecommendations);
                        requests.addAll(groupRecommendationRequest.itemsToPredict);
                    }

                    for (GroupMeasure groupMeasure : grouMeasures) {
                        double groupMeasureValue = groupMeasure.getMeasure(originalDatasetLoader, group);
                        groupLevelResults.setGroupMeasure(groupMeasure, groupMeasureValue);
                    }

                    for (GroupEvaluationMeasure evaluationMeasure : evaluationMeasures) {
                        Map<GroupOfUsers, Collection<Item>> _requests = new TreeMap<>();
                        _requests.put(group, requests);
                        Map<GroupOfUsers, Collection<Recommendation>> _recommendations = new TreeMap<>();
                        _recommendations.put(group, allPredictions);

                        List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs = Arrays.asList(
                                new SingleGroupRecommendationTaskInput(
                                        groupRecommenderSystem,
                                        originalDatasetLoader,
                                        recommendationModel,
                                        group,
                                        requests));

                        List<SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs = Arrays.asList(
                                new SingleGroupRecommendationTaskOutput(group, allPredictions, 0, 0)
                        );

                        GroupRecommenderSystemResult groupRecommendationResult = new GroupRecommenderSystemResult(
                                singleGroupRecommendationInputs,
                                singleGroupRecommendationOutputs,
                                GroupLevelCaseStudy.class.getSimpleName(), 0, 0, -1);

                        GroupEvaluationMeasureResult measureResult = evaluationMeasure.getMeasureResult(
                                groupRecommendationResult,
                                originalDatasetLoader,
                                testDatasetLoader.getRatingsDataset(),
                                relevanceCriteria,
                                trainingDatasetLoader,
                                testDatasetLoader);
                        groupLevelResults.setEvaluationMeasure(groupRecommenderSystem, evaluationMeasure, measureResult);
                    }
                }

                allResultsCaseStudy.get(group).put(split, groupLevelResults);
                long timeElapsed = c.getTotalElapsed();
                i++;

                {
                    //Línea del grupo
                    StringBuilder line = new StringBuilder();
                    line.append(group.toString());
                    line.append("\t").append(split);

                    for (GroupMeasure groupMeasure : grouMeasures) {
                        line.append("\t").append(groupLevelResults.getGroupMeasureValue(groupMeasure));
                    }

                    for (GroupEvaluationMeasure groupEvaluationMeasure : evaluationMeasures) {
                        for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {
                            GroupEvaluationMeasureResult groupMeasureResult = groupLevelResults.getEvaluationMeasureValue(groupRecommenderSystem, groupEvaluationMeasure);
                            line.append("\t").append(groupMeasureResult.getValue());
                        }
                    }

                    line.append("\t").append(DateCollapse.collapse(timeElapsed));
                    Global.showln(line.toString());
                }
            }

        }
    }
}
