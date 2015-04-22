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
 * @author Jorge Castro Gallardo
 *
 * @param <RecommenderSystemModel> Modelo del sistema de recomendación. Es el
 * modelo general utilizado para todos los grupos de usuarios.
 * @param <GroupModel> Modelo de cada grupo, específico al mismo.
 */
public abstract class GroupRecommenderSystemAdapter<RecommenderSystemModel, GroupModel>
        extends GenericRecommenderSystemAdapter<RecommenderSystemModel>
        implements GroupRecommenderSystem<RecommenderSystemModel, GroupModel> {

    public GroupRecommenderSystemAdapter() {
        super();
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_RECOMMENDER_SYSTEM;
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RecommenderSystemModel recommenderSystemModel, GroupModel groupModel, GroupOfUsers groupOfUsers, Integer... idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        return recommendOnly(datasetLoader, recommenderSystemModel, groupModel, groupOfUsers, new TreeSet<>(Arrays.asList(idItemList)));
    }
}
