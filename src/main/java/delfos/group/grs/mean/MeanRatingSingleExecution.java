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

import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
