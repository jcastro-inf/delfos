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
package delfos.group.grs.hesitant;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.utils.hesitant.HesitantValuation;
import delfos.utils.hesitant.similarity.HesitantSimilarity;

/**
 * Clase que almacena los datos necesarios para ejecutar paralelamente el
 * cálculo de la similitud con un vecino.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 14-Noviembre-2013
 */
public class HesitantKnnNeighborSimilarityTask extends Task {

    public final GroupOfUsers groupOfUsers;
    public final User neighborUser;
    public DatasetLoader<? extends Rating> datasetLoader;
    public Neighbor neighbor = null;

    public final HesitantValuation<Item, Double> groupModel;
    public HesitantSimilarity hesitantSimilarity;

    public HesitantKnnNeighborSimilarityTask(
            DatasetLoader<? extends Rating> datasetLoader,
            GroupOfUsers groupOfUsers, HesitantValuation<Item, Double> groupModel,
            User neighborUser, HesitantSimilarity similarity) throws UserNotFound {
        this.datasetLoader = datasetLoader;
        this.groupModel = groupModel;

        this.groupOfUsers = groupOfUsers;
        this.neighborUser = neighborUser;
        this.hesitantSimilarity = similarity;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("group ----------> ").append(groupOfUsers.getIdMembers()).append("\n");
        str.append("idNeighbor -----> ").append(neighborUser).append("\n");
        str.append("rs -------------> ").append(hesitantSimilarity.getClass().getSimpleName()).append("\n");

        return str.toString();
    }

    public void setNeighbor(Neighbor neighbor) {
        this.neighbor = neighbor;
        hesitantSimilarity = null;
        datasetLoader = null;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }
}
