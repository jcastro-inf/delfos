package delfos.group.casestudy.parallelisation;

import delfos.common.Chronometer;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 *
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 30-May-2013
 */
public class SingleGroupRecommendationFunction implements Function<SingleGroupRecommendationTaskInput, SingleGroupRecommendationTaskOutput> {

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

        SingleGroupRecommendationTaskOutput groupRecommendationTaskOutput = new SingleGroupRecommendationTaskOutput(groupOfUsers, recommendations, buildTime, recommendationTime);

        return groupRecommendationTaskOutput;
    }

}
