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
package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;

/**
 * Computes the similarity between the user and the neighbour, taking into
 * account the RS parameters.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class KnnMemoryNeighborTask {

    public final User user;
    public final User neighbor;
    public KnnCollaborativeRecommender rs;
    public DatasetLoader<? extends Rating> datasetLoader;

    public KnnMemoryNeighborTask(
            DatasetLoader<? extends Rating> datasetLoader,
            User user,
            User neighbor,
            KnnCollaborativeRecommender rs) throws UserNotFound {

        this.datasetLoader = datasetLoader;
        this.neighbor = neighbor;

        this.rs = rs;
        this.user = user;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("idUser ---------> ").append(user).append("\n");
        str.append("idNeighbor -----> ").append(neighbor).append("\n");
        str.append("rs -------------> ").append(rs.getAlias()).append("\n");
        str.append("\n").append(rs.getNameWithParameters());

        return str.toString();
    }
}
