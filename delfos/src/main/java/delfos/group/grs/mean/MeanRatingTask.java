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
package delfos.group.grs.mean;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
