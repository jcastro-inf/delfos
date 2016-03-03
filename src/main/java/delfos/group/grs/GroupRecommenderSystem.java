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
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.GenericRecommenderSystem;
import java.util.Set;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 13-Feb-2013
 * @param <RecommendationModel>
 * @param <GroupModel>
 */
public interface GroupRecommenderSystem<RecommendationModel, GroupModel> extends GenericRecommenderSystem<RecommendationModel> {

    /**
     * Método utilizado para construir el modelo de los usuarios que pertenecen
     * al grupo indicado.
     *
     * <p>
     * <p>
     * NOTA: Por defecto este método está vacío, por lo que los sistemas de
     * recomendación que lo necesiten deben sobreescribirlo.
     *
     * @param <RatingType>
     * @param datasetLoader
     * @param RecommendationModel
     * @param groupOfUsers Grupo para el que se genera su modelo asociado
     * @return
     * @throws UserNotFound Cuando algún usuario del el usuario del grupo no
     * existe
     * @throws delfos.common.exceptions.ratings.NotEnoughtUserInformation
     *
     */
    public <RatingType extends Rating> GroupModel buildGroupModel(
            DatasetLoader<RatingType> datasetLoader,
            RecommendationModel RecommendationModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation;

    /**
     * Devuelve las recomendacione para el grupo de usuarios indicado de entre
     * las opciones disponibles indicadas.
     *
     * @param <RatingType>
     * @param datasetLoader
     * @param recommendationModel
     * @param groupModel
     * @param groupOfUsers Grupo de usuarios para el que se realiza la
     * recomendación
     * @param candidateItems Conjunto de opciones disponibles para recomendar
     * @return Lista ordenada de recomendaciones para el grupo
     * @throws UserNotFound Cuando algún usuario del el usuario del grupo no
     * existe
     * @throws delfos.common.exceptions.ratings.NotEnoughtUserInformation
     */
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, RecommendationModel recommendationModel, GroupModel groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation;

}
