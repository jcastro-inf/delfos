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
package delfos.group.grs.filtered;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.PseudoUserRatingsDataset_manyPseudoUsers;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.GroupModelRatingsPreFilter;
import delfos.group.grs.filtered.filters.GroupRatingsFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.explanation.NestedExplanation;
import delfos.rs.recommendation.Recommendation;

/**
 * Implementa la unión de un sistema de recomendación con un algoritmo de
 * prefiltrado de las valoraciones de los usuarios.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 07-May-2013
 */
public class GroupRecommenderSystemWithPreFilter extends GroupRecommenderSystemAdapter<Object, GroupModelWithExplanation<GroupModelRatingsPreFilter, ? extends Object>> {

    private static final long serialVersionUID = 1L;
    /**
     * Sistema de recomendación a grupos que utiliza.
     */
    public static final Parameter GROUP_RECOMMENDER_SYSTEM = new Parameter(
            "GROUP_RECOMMENDER_SYSTEM",
            new RecommenderSystemParameterRestriction(new AggregationOfIndividualRatings(new KnnModelBasedCFRS(), new Mean()), GroupRecommenderSystem.class),
            "Sistema de recomendación a grupos que utiliza.");
    /**
     * Filtro de valoraciones de grupo que se aplica antes de realizar la
     * recomendación.
     */
    public static final Parameter GROUP_RATINGS_PRE_FILTER = new Parameter(
            "GROUP_RATINGS_PRE_FILTER",
            new ParameterOwnerRestriction(GroupRatingsFilter.class, new OutliersRatingsFilter(0.5, 0.2)),
            "Filtro de valoraciones de grupo que se aplica antes de realizar la recomendación.");

    public GroupRecommenderSystemWithPreFilter() {
        super();
        addParameter(GROUP_RATINGS_PRE_FILTER);
        addParameter(GROUP_RECOMMENDER_SYSTEM);

        ParameterListener keepMaintainRecommender = new ParameterListener() {
            private GroupRecommenderSystem grs = null;

            private RecommendationModelBuildingProgressListener bpl
                    = (String actualJob, int percent, long remainingTime) -> {
                        fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
                    };

            @Override
            public void parameterChanged() {
                if (grs == null) {
                    grs = GroupRecommenderSystemWithPreFilter.this.getGroupRecommenderSystem();
                    grs.addRecommendationModelBuildingProgressListener(bpl);
                }
                if (getGroupRecommenderSystem() == grs) {
                    //Es el mismo, no hacer nada.
                } else {
                    grs.removeRecommendationModelBuildingProgressListener(bpl);
                    grs = getGroupRecommenderSystem();
                    grs.addRecommendationModelBuildingProgressListener(bpl);
                }
            }
        };

        this.addParammeterListener(keepMaintainRecommender);
    }

    public GroupRecommenderSystemWithPreFilter(GroupRecommenderSystem grs, GroupRatingsFilter filter) {
        this();

        setParameterValue(GROUP_RECOMMENDER_SYSTEM, grs);
        setParameterValue(GROUP_RATINGS_PRE_FILTER, filter);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getGroupRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public Object buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        RecommendationModelBuildingProgressListener buildListener = this::fireBuildingProgressChangedEvent;

        getGroupRecommenderSystem().addRecommendationModelBuildingProgressListener(buildListener);
        Object build = getGroupRecommenderSystem().buildRecommendationModel(datasetLoader);
        getGroupRecommenderSystem().removeRecommendationModelBuildingProgressListener(buildListener);

        return build;
    }

    @Override
    public GroupModelWithExplanation<GroupModelRatingsPreFilter, ? extends Object> buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, Object RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        Map<Integer, Map<Integer, Rating>> filteredRatings = getGroupRatingsFilter().getFilteredRatings(datasetLoader.getRatingsDataset(), groupOfUsers);

        checkFilteredRatings(datasetLoader, groupOfUsers, filteredRatings);

        //Construyo un dataset modificado para la construcción del modelo interno.
        PseudoUserRatingsDataset_manyPseudoUsers<Rating> modifiedDataset = new PseudoUserRatingsDataset_manyPseudoUsers<>(datasetLoader.getRatingsDataset());

        Map<Integer, Integer> pseudoMembers = new TreeMap<>();

        for (int idUser : filteredRatings.keySet()) {
            Map<Integer, Rating> thisUserFilteredRatings = filteredRatings.get(idUser);

            //Genero un usuario para el miembro actual
            int idPseudoUser = modifiedDataset.setPseudoUserRatings(
                    thisUserFilteredRatings,
                    idUser);
            pseudoMembers.put(idPseudoUser, idUser);

        }

        Object innerGRSGroupModel = getGroupRecommenderSystem().buildGroupModel(new DatasetLoaderGivenRatingsDataset(datasetLoader, modifiedDataset),
                RecommendationModel,
                new GroupOfUsers(pseudoMembers.keySet()));

        final Object explanation;
        if (innerGRSGroupModel instanceof GroupModelWithExplanation) {
            GroupModelWithExplanation groupModelWithExplanation = (GroupModelWithExplanation) innerGRSGroupModel;
            explanation = groupModelWithExplanation.getExplanation();
        } else {
            explanation = "No explanation for " + getGroupRecommenderSystem().getAlias() + ".";
        }
        return new GroupModelWithExplanation<>(new GroupModelRatingsPreFilter(filteredRatings, innerGRSGroupModel), new NestedExplanation<>("No explanation for '" + this.getName() + "' grs", explanation));
    }

    public void checkFilteredRatings(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers, Map<Integer, Map<Integer, Rating>> filteredRatings) {
        Set<Integer> usersWithoutRatingsDueToFiltering = new TreeSet<>();

        for (int idUser : groupOfUsers.getIdMembers()) {
            if (!filteredRatings.containsKey(idUser) || filteredRatings.get(idUser).isEmpty()) {
                usersWithoutRatingsDueToFiltering.add(idUser);
                Global.showWarning("Users " + idUser + " has no ratings due to filter '" + getGroupRatingsFilter().getAlias() + "'.");
            }
        }

        if (!usersWithoutRatingsDueToFiltering.isEmpty()) {
            Global.showWarning("Users " + usersWithoutRatingsDueToFiltering + " has no ratings due to filter '" + getGroupRatingsFilter().getAlias() + "'.");
        }
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, Object RecommendationModel, GroupModelWithExplanation<GroupModelRatingsPreFilter, ? extends Object> groupModelWithExplanation, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        Object innerGRSGroupModel = groupModelWithExplanation.getGroupModel().getInnerGRSGroupModel();

        PseudoUserRatingsDataset_manyPseudoUsers<Rating> modifiedDataset
                = new PseudoUserRatingsDataset_manyPseudoUsers<>(datasetLoader.getRatingsDataset());

        Map<Integer, Integer> pseudoMembers = new TreeMap<>();

        for (int idUser : groupModelWithExplanation.getGroupModel().getRatings().keySet()) {
            //Genero un usuario para el miembro actual
            int idPseudoUser = modifiedDataset.setPseudoUserRatings(
                    groupModelWithExplanation.getGroupModel().getRatings().get(idUser),
                    idUser);
            pseudoMembers.put(idPseudoUser, idUser);
        }

        try {
            Collection<Recommendation> recommendations = getGroupRecommenderSystem().recommendOnly(new DatasetLoaderGivenRatingsDataset(datasetLoader, modifiedDataset),
                    RecommendationModel,
                    innerGRSGroupModel,
                    new GroupOfUsers(pseudoMembers.keySet()),
                    candidateItems);
            return recommendations;
        } catch (UserNotFound ex) {
            throw new UserNotFound(pseudoMembers.get(ex.getIdUser()), ex);
        }
    }

    private GroupRecommenderSystem getGroupRecommenderSystem() {
        return (GroupRecommenderSystem) getParameterValue(GROUP_RECOMMENDER_SYSTEM);
    }

    private GroupRatingsFilter getGroupRatingsFilter() {
        return (GroupRatingsFilter) getParameterValue(GROUP_RATINGS_PRE_FILTER);
    }
}
