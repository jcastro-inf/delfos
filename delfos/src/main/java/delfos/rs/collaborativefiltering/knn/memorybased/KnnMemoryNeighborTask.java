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

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.profile.Neighbor;

/**
 * Clase que almacena los datos necesarios para ejecutar paralelamente el
 * cálculo de la similitud con un vecino.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 14-Noviembre-2013
 */
public class KnnMemoryNeighborTask extends Task {

    public final User user;
    public final User neighborUser;
    public KnnMemoryBasedCFRS rs;
    public DatasetLoader<? extends Rating> datasetLoader;
    public Neighbor neighbor = null;

    public KnnMemoryNeighborTask(DatasetLoader<? extends Rating> datasetLoader, User user, User neighborUser, KnnMemoryBasedCFRS rs) {
        this.datasetLoader = datasetLoader;
        this.user = user;
        this.neighborUser = neighborUser;
        this.rs = rs;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("idUser ---------> ").append(user.getId()).append("(").append(user.getName()).append("\n");
        str.append("idNeighbor -----> ").append(neighborUser.getId()).append("(").append(neighborUser.getName()).append("\n");
        str.append("rs -------------> ").append(rs.getAlias()).append("\n");
        str.append("\n").append(rs.getNameWithParameters());

        return str.toString();
    }

    public void setNeighbor(Neighbor neighbor) {
        this.neighbor = neighbor;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }
}
