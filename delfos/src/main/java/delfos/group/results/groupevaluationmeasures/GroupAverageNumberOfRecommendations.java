package delfos.group.results.groupevaluationmeasures;

import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;

/**
 * Medida de evaluación para calcular el número de medio de predicciones que se
 * calcularon por grupo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (26-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE_ForGroups
 */
public class GroupAverageNumberOfRecommendations extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative mean = new MeanIterative();
        for (GroupOfUsers group : groupRecommenderSystemResult) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(group).getRecommendations();

            mean.addValue(groupRecommendations.size());

        }
        return new GroupEvaluationMeasureResult(this, mean.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
