package delfos.group.grs.mean;

import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;

/**
 *
* @author Jorge Castro Gallardo
 */
public class MeanRatingSingleExecution implements SingleTaskExecute<MeanRatingTask> {

    @Override
    public void executeSingleTask(MeanRatingTask task) {
        MeanRating meanRating;
        try {
            meanRating = new MeanRating(
                    task.getIdItem(),
                    task.getRatingsDataset().getMeanRatingItem(task.getIdItem()));
            task.setMeanRating(meanRating);
        } catch (ItemNotFound ex) {
            Global.showWarning("Item " + ex.idItem + " does not have ratings.");
            task.setMeanRating(new MeanRating(task.getIdItem(), task.getRatingsDataset().getRatingsDomain().min()));
        }
    }
}
