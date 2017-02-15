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

import delfos.common.Chronometer;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.recommendation.RecommendationsToUser;
import delfos.utils.algorithm.progress.ProgressChangedController;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * Encapsulates the recommendation to a user.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @param <RecommendationModel>
 * @param <RatingType>
 */
public class RecommendationFunction<RecommendationModel extends Object, RatingType extends Rating>
        implements Function<RecommendationTaskInput<RecommendationModel, RatingType>, RecommendationTaskOutput> {

    private final ProgressChangedController recommendationProgress;

    public RecommendationFunction() {
        this.recommendationProgress = null;
    }

    public RecommendationFunction(ProgressChangedController recommendationProgress) {
        this.recommendationProgress = recommendationProgress;
    }

    @Override
    public RecommendationTaskOutput apply(RecommendationTaskInput<RecommendationModel, RatingType> task) {

        Object groupModel;

        User user = task.getUser();

        final DatasetLoader<? extends Rating> datasetLoader = task.getDatasetLoader();
        final RecommendationModel recommendationModel = task.getRecommendationModel();
        final Set<Item> itemsRequested = task.getItemsRequested();

        if (itemsRequested.isEmpty()) {
            RecommendationsToUser recommendations = new RecommendationsToUser(user, Collections.EMPTY_LIST);
            RecommendationTaskOutput recommendationTaskOutput
                    = new RecommendationTaskOutput(
                            user, recommendations, -1);

            return recommendationTaskOutput;
        }

        RecommendationsToUser recommendations = null;
        long buildTime = -1;
        long recommendationTime = -1;

        {
            Chronometer chronometer = new Chronometer();
            recommendations = task.getRecommenderSystem().recommendToUser(datasetLoader, recommendationModel, user, itemsRequested);
            recommendationTime = chronometer.getTotalElapsed();
        }

        notifyProgress();

        RecommendationTaskOutput recommendationTaskOutput
                = new RecommendationTaskOutput(user, recommendations, recommendationTime);

        return recommendationTaskOutput;
    }

    private void notifyProgress() {
        if (this.recommendationProgress != null) {
            recommendationProgress.setTaskFinished();
        }
    }

}
