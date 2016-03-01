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
package delfos.group.casestudy.parallelisation;

import delfos.common.Chronometer;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import delfos.utils.algorithm.progress.ProgressChangedController;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 *
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 30-May-2013
 */
public class SingleGroupRecommendationFunction implements Function<SingleGroupRecommendationTaskInput, SingleGroupRecommendationTaskOutput> {

    private final ProgressChangedController recommendationProgress;

    public SingleGroupRecommendationFunction() {
        this.recommendationProgress = null;
    }

    @Override
    public SingleGroupRecommendationTaskOutput apply(SingleGroupRecommendationTaskInput task) {

        Object groupModel;

        final GroupOfUsers groupOfUsers = task.getGroupOfUsers();

        final DatasetLoader<? extends Rating> datasetLoader = task.getDatasetLoader();
        final Object recommendationModel = task.getRecommendationModel();
        final Set<Integer> itemsRequested = task.getItemsRequested();

        Collection<Recommendation> recommendations = Collections.EMPTY_LIST;
        long buildTime = -1;
        long recommendationTime = -1;

        try {

            {
                Chronometer chronometer = new Chronometer();
                groupModel = task.getGroupRecommenderSystem().buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);
                buildTime = chronometer.getTotalElapsed();
            }

            {
                Chronometer chronometer = new Chronometer();
                recommendations = task.getGroupRecommenderSystem().recommendOnly(datasetLoader, recommendationModel,
                        groupModel, groupOfUsers, itemsRequested);
                recommendationTime = chronometer.getTotalElapsed();
            }

        } catch (NotEnoughtUserInformation ex) {

        }

        notifyProgress();

        SingleGroupRecommendationTaskOutput groupRecommendationTaskOutput = new SingleGroupRecommendationTaskOutput(groupOfUsers, recommendations, buildTime, recommendationTime);

        return groupRecommendationTaskOutput;
    }

    private void notifyProgress() {
        if (this.recommendationProgress != null) {
            recommendationProgress.setTaskFinished();
        }
    }

}
