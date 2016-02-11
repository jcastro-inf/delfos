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
package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation;

import delfos.common.parallelwork.SingleTaskExecute;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.useruser.UserUserSimilarity;

public final class KnnMultiCorrelation_SingleNeighborCalculator implements SingleTaskExecute<KnnMultiCorrelation_Task> {

    public KnnMultiCorrelation_SingleNeighborCalculator() {
        super();
    }

    @Override
    public void executeSingleTask(KnnMultiCorrelation_Task task) {
        int idUser = task.idUser;
        int idNeighbor = task.idNeighbor;
        KnnMultiCorrelation rs = task.rs;
        RatingsDataset<? extends Rating> ratingsDataset = task.datasetLoader.getRatingsDataset();

        if (idUser == idNeighbor) {
            return;
        }
        UserUserSimilarity userUserSimilarity = (UserUserSimilarity) rs.getParameterValue(KnnMultiCorrelation.MULTI_CORRELATION_SIMILARITY_MEASURE);

        double similarity = userUserSimilarity.similarity(task.datasetLoader, idUser, idNeighbor);
        Neighbor neighbor = new Neighbor(RecommendationEntity.USER, idNeighbor, similarity);
        task.setNeighbor(neighbor);

    }
}
