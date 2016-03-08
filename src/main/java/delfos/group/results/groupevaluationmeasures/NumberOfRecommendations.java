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
package delfos.group.results.groupevaluationmeasures;

import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;

/**
 * Computes the average number of recommendations per group.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class NumberOfRecommendations extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative meanRecommendationsPerGroup = new MeanIterative();

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            GroupRecommendations groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            if (groupRecommendations == null) {
                throw new IllegalStateException("The group " + groupOfUsers + " has null recommendations.");
            } else {
                long recommendedThisGroup = groupRecommendations.getRecommendations().stream()
                        .filter(recommendation -> recommendation != null)
                        .filter(recommendation -> !Double.isNaN(recommendation.getPreference().doubleValue()))
                        .count();

                meanRecommendationsPerGroup.addValue(recommendedThisGroup);
            }
        }
        return new GroupEvaluationMeasureResult(this, meanRecommendationsPerGroup.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
