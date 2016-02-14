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
package delfos.group.grs;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.GenericRecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Sistema de recomendación para grupos de usuarios. Esta clase define los
 * métodos de un sistema de recomendación a grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @param <RecommendationModel> Modelo del sistema de recomendación. Es el
 * modelo general utilizado para todos los grupos de usuarios.
 * @param <GroupModel> Modelo de cada grupo, específico al mismo.
 */
public abstract class GroupRecommenderSystemAdapter<RecommendationModel, GroupModel>
        extends GenericRecommenderSystemAdapter<RecommendationModel>
        implements GroupRecommenderSystem<RecommendationModel, GroupModel> {

    public GroupRecommenderSystemAdapter() {
        super();
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_RECOMMENDER_SYSTEM;
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel RecommendationModel, GroupModel groupModel, GroupOfUsers groupOfUsers, Integer... candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        return recommendOnly(datasetLoader, RecommendationModel, groupModel, groupOfUsers, new TreeSet<>(Arrays.asList(candidateItems)));
    }
}
