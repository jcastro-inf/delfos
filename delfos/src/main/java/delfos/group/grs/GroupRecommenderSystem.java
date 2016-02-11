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
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Jorge Castro Gallardo
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
     * @param datasetLoader
     * @param RecommendationModel
     * @param groupOfUsers Grupo para el que se genera su modelo asociado
     * @return
     * @throws UserNotFound Cuando algún usuario del el usuario del grupo no
     * existe
     *
     */
    public GroupModel buildGroupModel(
            DatasetLoader<? extends Rating> datasetLoader,
            RecommendationModel RecommendationModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation;

    /**
     * Devuelve las recomendacione para el grupo de usuarios indicado de entre
     * las opciones disponibles indicadas.
     *
     * @param groupOfUsers Grupo de usuarios para el que se realiza la
     * recomendación
     * @param candidateItems Conjunto de opciones disponibles para recomendar
     * @return Lista ordenada de recomendaciones para el grupo
     * @throws UserNotFound Cuando algún usuario del el usuario del grupo no
     * existe
     */
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, RecommendationModel RecommendationModel, GroupModel groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation;

    /**
     * Devuelve las recomendacione para el grupo de usuarios indicado de entre
     * las opciones disponibles indicadas.
     *
     * @param groupOfUsers Grupo de usuarios para el que se realiza la
     * recomendación
     * @param candidateItems Conjunto de opciones disponibles para recomendar
     * @return Lista ordenada de recomendaciones para el grupo
     * @throws UserNotFound Cuando algún usuario del el usuario del grupo no
     * existe
     */
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader,
            RecommendationModel RecommendationModel,
            GroupModel groupModel,
            GroupOfUsers groupOfUsers,
            Integer... candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation;
}
