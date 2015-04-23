package delfos.group.grouplevelcasestudy.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.group.grouplevelcasestudy.GroupLevelResults;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupRecommendationRequest;
import delfos.rs.recommendation.Recommendation;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 04-Jun-2013
 */
public class SingleGroupTaskExecute implements SingleTaskExecute<SingleGroupTask> {

    @Override
    public void executeSingleTask(SingleGroupTask task) {
        GroupLevelResults[] groupResults = null;
        try {
            groupResults = computeGroup(task.getSeed(), task.getValidationTechnique(), task.getDatasetLoader(), task.getGroup(), task.getGroupRecommenderSystems(), task.getPredictionProtocol(), task.getGrouMeasures(), task.getEvaluationMeasures());
        } catch (NotEnoughtUserInformation ex) {
            //Cannot be computed, lack of information from group.
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        }

        task.setGroupLevelResults(groupResults);
    }

    private GroupLevelResults[] computeGroup(
            long seed,
            GroupValidationTechnique validationTechnique,
            DatasetLoader<? extends Rating> datasetLoader,
            GroupOfUsers group,
            GroupRecommenderSystem[] groupRecommenderSystems,
            GroupPredictionProtocol predictionProtocol,
            GroupMeasure[] grouMeasures,
            Collection<GroupEvaluationMeasure> evaluationMeasures)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {

        GroupOfUsers[] groups = new GroupOfUsers[1];
        groups[0] = group;
        PairOfTrainTestRatingsDataset[] pairs = validationTechnique.shuffle(datasetLoader, groups);

        validationTechnique.setSeedValue(seed);
        predictionProtocol.setSeedValue(seed);
        int numSplit = validationTechnique.getNumberOfSplits();

        GroupLevelResults[] ret = new GroupLevelResults[numSplit];

        for (int split = 0; split < numSplit; split++) {

            GroupLevelResults groupLevelResults = new GroupLevelResults(group);
            ret[split] = groupLevelResults;

            DatasetLoader<? extends Rating> trainingDatasetLoader = pairs[split].getTrainingDatasetLoader();
            DatasetLoader<? extends Rating> testDatasetLoader = pairs[split].getTestDatasetLoader();

            for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {

                Object RecommendationModel = groupRecommenderSystem.buildRecommendationModel(trainingDatasetLoader);
                Collection<Recommendation> allPredictions = new ArrayList<>();
                List<Integer> requests = new ArrayList<>();

                Collection<GroupRecommendationRequest> allRequests = predictionProtocol.getGroupRecommendationRequests(trainingDatasetLoader, testDatasetLoader, group);
                for (GroupRecommendationRequest groupRecommendationRequest : allRequests) {

                    Object groupModel = groupRecommenderSystem.buildGroupModel(
                            groupRecommendationRequest.predictionPhaseDatasetLoader,
                            RecommendationModel,
                            group);

                    Collection<Recommendation> groupRecommendations = groupRecommenderSystem.recommendOnly(
                            groupRecommendationRequest.predictionPhaseDatasetLoader,
                            RecommendationModel,
                            groupModel,
                            group,
                            groupRecommendationRequest.itemsToPredict);

                    allPredictions.addAll(groupRecommendations);
                    requests.addAll(groupRecommendationRequest.itemsToPredict);
                }

                for (GroupMeasure groupMeasure : grouMeasures) {
                    double groupMeasureValue = groupMeasure.getMeasure(datasetLoader, group);
                    groupLevelResults.setGroupMeasure(groupMeasure, groupMeasureValue);
                }

                for (GroupEvaluationMeasure evaluationMeasure : evaluationMeasures) {
                    Map<GroupOfUsers, Collection<Integer>> _requests = new TreeMap<>();
                    _requests.put(group, requests);
                    Map<GroupOfUsers, Collection<Recommendation>> _recommendations = new TreeMap<>();
                    _recommendations.put(group, allPredictions);
                    GroupRecommendationResult groupRecommendationResult = new GroupRecommendationResult(0, 0, 0, 1, _requests, _recommendations, groupRecommenderSystem.getAlias());
                    GroupMeasureResult measureResult = evaluationMeasure.getMeasureResult(groupRecommendationResult, testDatasetLoader.getRatingsDataset(), datasetLoader.getDefaultRelevanceCriteria());
                    groupLevelResults.setEvaluationMeasure(groupRecommenderSystem, evaluationMeasure, measureResult);
                }
            }
        }
        return ret;
    }
}
