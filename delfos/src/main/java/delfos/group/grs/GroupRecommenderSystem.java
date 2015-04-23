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
     * @param idItemList Conjunto de opciones disponibles para recomendar
     * @return Lista ordenada de recomendaciones para el grupo
     * @throws UserNotFound Cuando algún usuario del el usuario del grupo no
     * existe
     */
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, RecommendationModel RecommendationModel, GroupModel groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> idItemList)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation;

    /**
     * Devuelve las recomendacione para el grupo de usuarios indicado de entre
     * las opciones disponibles indicadas.
     *
     * @param groupOfUsers Grupo de usuarios para el que se realiza la
     * recomendación
     * @param idItemList Conjunto de opciones disponibles para recomendar
     * @return Lista ordenada de recomendaciones para el grupo
     * @throws UserNotFound Cuando algún usuario del el usuario del grupo no
     * existe
     */
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader,
            RecommendationModel RecommendationModel,
            GroupModel groupModel,
            GroupOfUsers groupOfUsers,
            Integer... idItemList)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation;
}
