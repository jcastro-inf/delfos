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

import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTask;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTaskExecutor;
import delfos.factories.AggregationOperatorFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.filtered.filters.GroupRatingsFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Implementa la unión de un sistema de recomendación a individuos con un
 * algoritmo de filtrado de las predicciones de los usuarios. Finalmente agrega
 * con una función de agregación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 22-May-2013
 */
public class GroupRecommenderSystemWithPostFilter extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupOfUsers> {

    private static final long serialVersionUID = 1L;
    /**
     * Sistema de recomendación a individuos que utiliza.
     */
    public static final Parameter RECOMMENDER_SYSTEM = new Parameter(
            "recommenderSystem",
            new RecommenderSystemParameterRestriction(new KnnModelBasedCFRS(), RecommenderSystem.class),
            "Sistema de recomendación a grupos que utiliza.");
    /**
     * Filtro de valoraciones de grupo que se aplica antes de realizar la
     * recomendación.
     */
    public static final Parameter FILTER = new Parameter(
            "filter",
            new ParameterOwnerRestriction(GroupRatingsFilter.class, new OutliersRatingsFilter(0.5, 0.2)),
            "Filtro que se aplica a las predicciones antes de agregarlas.");
    /**
     * Especifica la técnica de agregación para agregar los ratings de los
     * usuarios y formar el perfil del grupo.
     */
    public static final Parameter AGGREGATION_OPERATOR = new Parameter(
            "aggregationOperator",
            new ObjectParameter(AggregationOperatorFactory.getInstance().getAllAggregationOperators(), new Mean()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo.");

    public GroupRecommenderSystemWithPostFilter() {
        super();
        addParameter(FILTER);
        addParameter(AGGREGATION_OPERATOR);
        addParameter(RECOMMENDER_SYSTEM);

        ParameterListener keepMaintainRecommender = new ParameterListener() {
            private RecommenderSystem rs = null;
            private RecommendationModelBuildingProgressListener bpl = new RecommendationModelBuildingProgressListener() {
                @Override
                public void buildingProgressChanged(String actualJob, int percent, long remainingTime) {
                    fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
                }
            };

            @Override
            public void parameterChanged() {
                if (rs == null) {
                    rs = GroupRecommenderSystemWithPostFilter.this.getRecommenderSystem();
                    rs.addRecommendationModelBuildingProgressListener(bpl);
                }
                if (getRecommenderSystem() == rs) {
                    //Es el mismo, no hacer nada.
                } else {
                    rs.removeRecommendationModelBuildingProgressListener(bpl);
                    rs = getRecommenderSystem();
                    rs.addRecommendationModelBuildingProgressListener(bpl);
                }
            }
        };

        this.addParammeterListener(keepMaintainRecommender);
    }

    public GroupRecommenderSystemWithPostFilter(RecommenderSystem rs, GroupRatingsFilter filter, AggregationOperator aggregationOperator) {
        this();

        setParameterValue(RECOMMENDER_SYSTEM, rs);
        setParameterValue(FILTER, filter);
        setParameterValue(AGGREGATION_OPERATOR, aggregationOperator);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        Object build = getRecommenderSystem().buildRecommendationModel(datasetLoader);
        return new SingleRecommendationModel(build);
    }

    @Override
    public <RatingType extends Rating> GroupOfUsers buildGroupModel(DatasetLoader<RatingType> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return groupOfUsers;
    }

    @Override
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        Map<Integer, Map<Integer, Number>> listsWithoutFilter = groupOfUsers.getMembers()
                .parallelStream()
                .map(member -> new SingleUserRecommendationTask(getRecommenderSystem(), datasetLoader, RecommendationModel, member.getId(), candidateItems))
                .map(new SingleUserRecommendationTaskExecutor())
                .collect(Collectors.toMap(
                        recommendationsToMember -> recommendationsToMember.getUser().getId(),
                        recommendationsToMember -> {
                            return recommendationsToMember.getRecommendations().parallelStream()
                            .collect(Collectors.toMap(
                                    recommendation -> recommendation.getItem().getId(),
                                    recommendation -> recommendation.getPreference()));

                        }));

        Map<Integer, Map<Integer, Number>> filteredLists = filterLists(getFilter(), listsWithoutFilter);

        Collection<Recommendation> ret = aggregateLists(getAggregationOperator(), filteredLists);

        {
            //Muestro las listas individuales y agregadas, para depuración.
            Map<Integer, Map<Integer, Number>> all = new TreeMap<>();
            all.putAll(listsWithoutFilter);

            Map<Integer, Number> aggregateListNotFiltered = new TreeMap<>();
            Collection<Recommendation> retNoFilter = aggregateLists(getAggregationOperator(), listsWithoutFilter);
            retNoFilter.stream().forEach((r) -> {
                aggregateListNotFiltered.put(r.getIdItem(), r.getPreference());
            });
            all.put(88888888, aggregateListNotFiltered);

            Map<Integer, Number> aggregateListFiltered = new TreeMap<>();
            ret.stream().forEach((r) -> {
                aggregateListFiltered.put(r.getIdItem(), r.getPreference());
            });
            all.put(99999999, aggregateListFiltered);
        }

        return new GroupRecommendations(groupOfUsers, ret);
    }

    public static Map<Integer, Map<Integer, Number>> filterLists(GroupRatingsFilter filter, Map<Integer, Map<Integer, Number>> toFilter) {
        Map<Integer, Map<Integer, Number>> filtered = filter.getFilteredRatings(toFilter);
        return filtered;
    }

    public static Collection<Recommendation> aggregateLists(AggregationOperator aggregationOperator, Map<Integer, Map<Integer, Number>> groupUtilityList) {

        //Reordeno las predicciones.
        Map<Integer, Collection<Number>> prediction_byItem = new TreeMap<>();
        for (int idUser : groupUtilityList.keySet()) {
            for (int idItem : groupUtilityList.get(idUser).keySet()) {
                Number preference = groupUtilityList.get(idUser).get(idItem);

                if (!prediction_byItem.containsKey(idItem)) {
                    prediction_byItem.put(idItem, new LinkedList<>());
                }

                prediction_byItem.get(idItem).add(preference);
            }
        }

        //agrego las predicciones de cada item.
        ArrayList<Recommendation> ret = new ArrayList<>();
        for (int idItem : prediction_byItem.keySet()) {
            Collection<Number> predictionsThisItem = prediction_byItem.get(idItem);

            if (prediction_byItem.isEmpty()) {
                continue;
            }

            double aggregateValue = aggregationOperator.aggregateValues(predictionsThisItem);
            ret.add(new Recommendation(idItem, aggregateValue));
        }

        //Ordeno las recomendaciones.
        Collections.sort(ret);

        return ret;
    }

    private RecommenderSystem<? extends Rating> getRecommenderSystem() {
        return (RecommenderSystem) getParameterValue(RECOMMENDER_SYSTEM);
    }

    private GroupRatingsFilter getFilter() {
        return (GroupRatingsFilter) getParameterValue(FILTER);
    }

    private AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);
    }
}
