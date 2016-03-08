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
package delfos.group.casestudy.parallelisation;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import java.util.Collections;
import java.util.Set;

/**
 * Stores the input of the calculation of the recommendations with the stream
 * function {@link SingleGroupRecommendationFunction}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class SingleGroupRecommendationTaskInput {

    private final GroupOfUsers groupOfUsers;
    private final GroupRecommenderSystem groupRecommenderSystem;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Object RecommendationModel;
    private final Set<Item> candidateItems;

    public SingleGroupRecommendationTaskInput(
            GroupRecommenderSystem groupRecommenderSystem,
            DatasetLoader<? extends Rating> datasetLoader,
            Object RecommendationModel,
            GroupOfUsers group,
            Set<Item> candidateItems) {
        this.groupOfUsers = group;
        this.groupRecommenderSystem = groupRecommenderSystem;
        this.datasetLoader = datasetLoader;
        this.RecommendationModel = RecommendationModel;
        this.candidateItems = candidateItems;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("group --------> ").append(groupOfUsers).append("\n");
        str.append("candidateItems ---> ").append(candidateItems).append("\n");
        str.append("grs ----------> ").append(groupRecommenderSystem.getAlias()).append("\n");
        str.append("\t").append(groupRecommenderSystem.getNameWithParameters()).append("\n");

        return str.toString();
    }

    public GroupOfUsers getGroupOfUsers() {
        return groupOfUsers;
    }

    public GroupRecommenderSystem getGroupRecommenderSystem() {
        return groupRecommenderSystem;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public Object getRecommendationModel() {
        return RecommendationModel;
    }

    public Set<Item> getItemsRequested() {
        return Collections.unmodifiableSet(candidateItems);
    }
}
