package delfos.group.grs.mean;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;

/**
* @author Jorge Castro Gallardo
 *
 * @version 1.0 9-Junio-2013
 */
public class MeanRatingTask extends Task {

    private final RatingsDataset<? extends Rating> ratingsDataset;
    private final int idItem;
    private MeanRating meanRating;

    /**
     *
     * @param ratingsDataset
     * @param idItem
     */
    public MeanRatingTask(RatingsDataset<? extends Rating> ratingsDataset, int idItem) {
        this.ratingsDataset = ratingsDataset;
        this.idItem = idItem;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("task-----> ").append(this.getClass().getName()).append("\n");
        str.append("idItem --> ").append(idItem).append("\n");

        return str.toString();
    }

    public void setMeanRating(MeanRating meanRating) {
        this.meanRating = meanRating;
    }

    public MeanRating getMeanRating() {
        return meanRating;
    }

    public RatingsDataset<? extends Rating> getRatingsDataset() {
        return ratingsDataset;
    }

    public int getIdItem() {
        return idItem;
    }

}
