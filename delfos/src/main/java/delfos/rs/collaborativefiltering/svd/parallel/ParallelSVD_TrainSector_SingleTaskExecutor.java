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
package delfos.rs.collaborativefiltering.svd.parallel;

import java.util.Map;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.rating.Rating;

/**
 *
 * @version 24-jul-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
