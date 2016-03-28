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
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Devuelve todos los productos del catálogo.
 *
 * @version 03-jun-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class AllCatalogItems extends RecommendationCandidatesSelector {

    @Override
    public Set<Item> candidateItems(DatasetLoader<? extends Rating> datasetLoader, User user) throws UserNotFound {
        return datasetLoader.getContentDataset().stream().collect(Collectors.toSet());
    }

    @Override
    public Set<Item> candidateItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws UserNotFound {
        return datasetLoader.getContentDataset().stream().collect(Collectors.toSet());
    }

}