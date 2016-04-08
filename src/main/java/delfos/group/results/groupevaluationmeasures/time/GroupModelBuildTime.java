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
package delfos.group.results.groupevaluationmeasures.time;

import delfos.common.statisticalfuncions.MeanIterative_Synchronized;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;

/**
 * Computes the average time that the group recommender system takes to build a
 * group model.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupModelBuildTime extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(GroupRecommenderSystemResult grsResult, DatasetLoader<? extends Rating> originalDatasetLoader, RelevanceCriteria relevanceCriteria, DatasetLoader<? extends Rating> trainingDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader) {
        MeanIterative_Synchronized mean = new MeanIterative_Synchronized();
        grsResult.getGroupsOfUsers().parallelStream()
                .forEach(GroupOfUsers -> {
                    SingleGroupRecommendationTaskOutput groupOutput = grsResult.getGroupOutput(GroupOfUsers);
                    mean.addValue(groupOutput.getBuildGroupModelTime());
                });

        double value = mean.getMean();
        return new GroupEvaluationMeasureResult(this, value);
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

}
