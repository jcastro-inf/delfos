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
package delfos.recommendationcandidates;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @version 03-jun-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class RecommendationCandidatesSelector extends ParameterOwnerAdapter {

    static final long serialVersionUID = 1l;
    public static RecommendationCandidatesSelector defaultValue = new OnlyNewItems();

    public RecommendationCandidatesSelector() {
        super();
    }

    @Deprecated
    public abstract Set<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, User user) throws UserNotFound;

    @Deprecated
    public abstract Set<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws UserNotFound;

    public Set<Item> candidateItemsNew(DatasetLoader<? extends Rating> datasetLoader, User user) throws UserNotFound {
        Set<Integer> candidateItems = candidateItems(datasetLoader, user);
        return candidateItems.parallelStream().map((idItem) -> ((ContentDatasetLoader) datasetLoader).getContentDataset().get(idItem)).collect(Collectors.toSet());
    }

    public Set<Item> candidateItemsNew(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws UserNotFound {
        Set<Integer> candidateItems = candidateItems(datasetLoader, groupOfUsers);
        return candidateItems.parallelStream().map((idItem) -> ((ContentDatasetLoader) datasetLoader).getContentDataset().get(idItem)).collect(Collectors.toSet());
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.RECOMMENDATION_CANDIDATES_SELECTOR;
    }
}
