package delfos.rs.collaborativefiltering.svd.parallel;

import java.util.Map;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.rating.Rating;

/**
 *
 * @version 24-jul-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class ParallelSVD_TrainSector_SingleTaskExecutor implements SingleTaskExecute<ParallelSVD_TrainSector_Task> {

    @Override
    public void executeSingleTask(ParallelSVD_TrainSector_Task task) {
        for (int idUser : task.getUsersSet()) {
            try {
                Map<Integer, ? extends Rating> userRatings = task.getRatingsDataset().getUserRatingsRated(idUser);
                for (Rating rating : userRatings.values()) {
                    if (task.getItemsSet().contains(rating.getIdItem())) {
                        ParallelSVD.trainModelWithThisRating(task.getAlgorithmParameters(), task.getParallelSVDModel(), rating, task.getFeature());
                    }
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
    }

}
