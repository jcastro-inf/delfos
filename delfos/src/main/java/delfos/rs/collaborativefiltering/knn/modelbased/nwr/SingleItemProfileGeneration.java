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
package delfos.rs.collaborativefiltering.knn.modelbased.nwr;

import java.util.List;
import java.util.Map;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.Global;
import delfos.common.parallelwork.SingleTaskExecute;

public final class SingleItemProfileGeneration implements SingleTaskExecute<KnnModelBasedCBRSTask> {

    public SingleItemProfileGeneration() {
        super();
    }

    @Override
    public void executeSingleTask(KnnModelBasedCBRSTask knnModelTask) {
        int idItem = knnModelTask.getIdItem();
        KnnModelBased_NWR rs = knnModelTask.getRecommenderSystem();

        //Calculo el perfil y lo guardo
        try {
            Map<Integer, ? extends Rating> get = knnModelTask.getRatingsDataset().getItemRatingsRated(idItem);
            if (!get.isEmpty()) {
                List<Neighbor> neighbors = rs.getNeighbors(knnModelTask.getRatingsDataset(), idItem);
                knnModelTask.setNeighbors(neighbors);
            }
        } catch (ItemNotFound ex) {
            Global.showWarning("Item " + idItem + " has no ratings.");
        }
    }
}
