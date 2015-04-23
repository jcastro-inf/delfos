package delfos.group.casestudy.parallelisation;

import delfos.common.Chronometer;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;

/**
 *
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 30-May-2013
 */
public class SingleGroupRecommendation implements SingleTaskExecute<SingleGroupRecommendationTask> {

    @Override
    public void executeSingleTask(SingleGroupRecommendationTask task) {

        Collection<Recommendation> recommendations = null;
        long buildTime = 0;
        long recommendationTime = 0;
        try {

            Chronometer chronometer = new Chronometer();
            Object groupModel = task.getGroupRecommenderSystem().buildGroupModel(task.getDatasetLoader(), task.getRecommendationModel(), task.getGroup());
            buildTime = chronometer.getTotalElapsed();

            chronometer.reset();
            recommendations = task.getGroupRecommenderSystem().recommendOnly(
                    task.getDatasetLoader(),
                    task.getRecommendationModel(),
                    groupModel,
                    task.getGroup(),
                    task.getCandidateItems());
            recommendationTime = chronometer.getTotalElapsed();

        } catch (UserNotFound | CannotLoadRatingsDataset | CannotLoadContentDataset | ItemNotFound | NotEnoughtUserInformation ex) {
        }
        task.setResults(recommendations, buildTime, recommendationTime);
    }
}
