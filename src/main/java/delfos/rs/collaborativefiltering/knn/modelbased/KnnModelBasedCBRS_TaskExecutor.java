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
package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.item.Item;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.List;

public final class KnnModelBasedCBRS_TaskExecutor implements SingleTaskExecute<KnnModelBasedCBRS_Task> {

    public KnnModelBasedCBRS_TaskExecutor() {
        super();
    }

    @Override
    public void executeSingleTask(KnnModelBasedCBRS_Task knnModelTask) {
        Item item = knnModelTask.getItem();
        KnnModelBasedCFRS rs = knnModelTask.getRecommenderSystem();

        List<Neighbor> neighbors = rs.getNeighbors(knnModelTask.getDatasetLoader(), item);
        knnModelTask.setNeighbors(neighbors);
    }
}
