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
package delfos.group.grs.aggregation;

import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTask;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTaskExecutor;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Implementa un sistema de recomendación a grupos que agrega las
 * recomendaciones de cada individuo para componer una lista de recomendaciones
 * para el grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 9-Mayo-2013
 */
public class AggregationOfIndividualRecommendations extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupOfUsers> {

    private static final long serialVersionUID = 1L;
    /**
     * Especifica el sistema de recomendación single user que se extiende para
     * ser usado en recomendación a grupos.
     */
    public static final Parameter SINGLE_USER_RECOMMENDER = new Parameter(
            "SINGLE_USER_RECOMMENDER",
            new RecommenderSystemParameterRestriction(new KnnMemoryBasedCFRS(), RecommenderSystem.class),
            "Especifica el sistema de recomendación single user que se extiende "
            + "para ser usaso en recomendación a grupos.");
    /**
     * Especifica la técnica de agregación para agregar los ratings de los
     * usuarios y formar el perfil del grupo.
     */
    public static final Parameter AGGREGATION_OPERATOR = new Parameter(
            "AGGREGATION_OPERATOR",
            new ParameterOwnerRestriction(AggregationOperator.class, new Mean()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo.");

    public static Set<Item> intersectionOfRecommendations(
            GroupRecommendations groupRecommendations,
            Collection<RecommendationsToUser> membersRecommendations) {
        return intersectionOfRecommendations(groupRecommendations.getRecommendations(),
                membersRecommendations.stream().collect(Collectors.toMap(
                                recommendationsToMember -> recommendationsToMember.getUser(),
                                recommendationsToMember -> recommendationsToMember.getRecommendations())));

    }

    public static Set<Item> intersectionOfRecommendations(Collection<Recommendation> groupRecommendations, Map<User, Collection<Recommendation>> membersRecommendations) {
        Set<Item> itemsIntersection = new TreeSet<>();
        for (Recommendation r : groupRecommendations) {
            itemsIntersection.add(r.getItem());
        }
        for (User idMember : membersRecommendations.keySet()) {
            Set<Item> thisUserIdItem_recommended = new TreeSet<>();
            membersRecommendations.get(idMember).stream().forEach((Recommendation r) -> {
                thisUserIdItem_recommended.add(r.getItem());
            });
            itemsIntersection.retainAll(thisUserIdItem_recommended);
        }
        itemsIntersection = Collections.unmodifiableSet(itemsIntersection);
        return itemsIntersection;
    }

    public static Collection<Recommendation> applyItemIntersection(Collection<Recommendation> recommendations,
            Set<Item> items) {
        Collection<Recommendation> recommendationsIntersected = new ArrayList<>(items.size());
        recommendations.stream().filter((Recommendation recommendation) -> (items.contains(recommendation.getItem()))).forEach((Recommendation recommendation) -> {
            recommendationsIntersected.add(recommendation);
        });
        return recommendationsIntersected;
    }

    public static RecommendationsToUser applyItemIntersection(RecommendationsToUser recommendations,
            Set<Item> items) {

        Collection<Recommendation> recommendationsIntersected = new ArrayList<>(items.size());
        recommendations.getRecommendations().stream().filter((Recommendation recommendation) -> (items.contains(recommendation.getItem()))).forEach((Recommendation recommendation) -> {
            recommendationsIntersected.add(recommendation);
        });

        return new RecommendationsToUser(recommendations.getUser(), recommendationsIntersected);
    }

    public static GroupRecommendations applyItemIntersection(GroupRecommendations recommendations,
            Set<Item> items) {

        Collection<Recommendation> recommendationsIntersected = new ArrayList<>(items.size());
        recommendations.getRecommendations().stream().filter((Recommendation recommendation) -> (items.contains(recommendation.getItem()))).forEach((Recommendation recommendation) -> {
            recommendationsIntersected.add(recommendation);
        });

        return new GroupRecommendations(recommendations.getGroupOfUsers(), recommendationsIntersected);
    }

    public static Map<User, Collection<Recommendation>> applyItemIntersection(Map<User, Collection<Recommendation>> usersRecommendations, Set<Integer> items) {
        Map<User, Collection<Recommendation>> userRecommendationsIntersected = new TreeMap<>();
        usersRecommendations.entrySet().stream().forEach((Map.Entry<User, Collection<Recommendation>> entry) -> {
            User idUser = entry.getKey();
            Collection<Recommendation> userRecommendations = entry.getValue();
            userRecommendationsIntersected.put(idUser, new ArrayList<>(items.size()));
            userRecommendations.stream().filter((Recommendation recommendation) -> (items.contains(recommendation.getIdItem()))).forEach((Recommendation recommendation) -> {
                userRecommendationsIntersected.get(idUser).add(recommendation);
            });
        });
        return userRecommendationsIntersected;
    }
    private AggregationOperator oldAggregationOperator = new Mean();

    public AggregationOfIndividualRecommendations() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(AGGREGATION_OPERATOR);

        addParammeterListener(() -> {
            AggregationOperator newAggregationOperator = (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = "AOI_Recommendations"
                    + "_" + oldAggregationOperator.getAlias();

            String newAliasNewParameters
                    = "AOI_Recommendations"
                    + "_" + newAggregationOperator.getAlias();

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldAggregationOperator = newAggregationOperator;
                setAlias(newAliasNewParameters);
            }
        });

        setAlias("AOI_Recommendations_" + oldAggregationOperator.getAlias());
    }

    public AggregationOfIndividualRecommendations(RecommenderSystem recommenderSystem, AggregationOperator aggregationOperator) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, recommenderSystem);
        setParameterValue(AGGREGATION_OPERATOR, aggregationOperator);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getSingleUserRecommender().isRatingPredictorRS();
    }

    public AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);
    }

    public RecommenderSystem getSingleUserRecommender() {
        return (delfos.rs.RecommenderSystem) getParameterValue(SINGLE_USER_RECOMMENDER);
    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        RecommendationModelBuildingProgressListener buildListener = (String actualJob, int percent, long remainingTime) -> {
            fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
        };

        getSingleUserRecommender().addRecommendationModelBuildingProgressListener(buildListener);
        Object build = getSingleUserRecommender().buildRecommendationModel(datasetLoader);
        getSingleUserRecommender().removeRecommendationModelBuildingProgressListener(buildListener);
        return new SingleRecommendationModel(build);
    }

    @Override
    public <RatingType extends Rating> GroupOfUsers buildGroupModel(DatasetLoader<RatingType> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return groupOfUsers;
    }

    @Override
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        RecommenderSystem singleUserRecommender = getSingleUserRecommender();
        Map<User, Collection<Recommendation>> recommendationsLists_byMember = performSingleUserRecommendationsOld(groupOfUsers.getIdMembers(), singleUserRecommender, datasetLoader, RecommendationModel, candidateItems);

        Collection<Recommendation> groupRecommendations = aggregateLists(getAggregationOperator(), recommendationsLists_byMember);
        return new GroupRecommendations(groupOfUsers, groupRecommendations);
    }

    public static Collection<Recommendation> aggregateLists(AggregationOperator aggregationOperator,
            Collection<RecommendationsToUser> groupUtilityList) {

        return aggregateLists(aggregationOperator,
                groupUtilityList.stream().collect(Collectors.toMap(
                                recommendationToUser -> recommendationToUser.getUser(),
                                recommendationToUser -> recommendationToUser.getRecommendations()
                        ))
        );
    }

    public static Collection<Recommendation> aggregateLists(AggregationOperator aggregationOperator,
            Map<User, Collection<Recommendation>> groupUtilityList) {

        //Reordeno las predicciones.
        Map<Integer, Collection<Number>> prediction_byItem = new TreeMap<>();
        for (User idUser : groupUtilityList.keySet()) {
            for (Recommendation r : groupUtilityList.get(idUser)) {
                int idItem = r.getIdItem();
                Number preference = r.getPreference();

                if (!prediction_byItem.containsKey(idItem)) {
                    prediction_byItem.put(idItem, new LinkedList<>());
                }

                prediction_byItem.get(idItem).add(preference);
            }
        }

        //agrego las predicciones de cada item.
        Collection<Recommendation> recommendations = new ArrayList<>();
        for (int idItem : prediction_byItem.keySet()) {
            Collection<Number> predictionsThisItem = prediction_byItem.get(idItem);

            if (prediction_byItem.isEmpty()) {
                continue;
            }

            double aggregateValue = aggregationOperator.aggregateValues(predictionsThisItem);
            recommendations.add(new Recommendation(idItem, aggregateValue));
        }

        return recommendations;
    }

    public static Collection<RecommendationsToUser>
            performSingleUserRecommendations(Collection<Integer> users,
                    RecommenderSystem<? extends Object> singleUserRecommender,
                    DatasetLoader<? extends Rating> datasetLoader,
                    SingleRecommendationModel recommendationModel,
                    Set<Item> candidateItems) throws UserNotFound {

        return performSingleUserRecommendationsOld(users, singleUserRecommender, datasetLoader, recommendationModel, candidateItems)
                .entrySet()
                .stream().map(entry -> new RecommendationsToUser(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static Map<User, Collection<Recommendation>> performSingleUserRecommendationsOld(Collection<Integer> users, RecommenderSystem<? extends Object> singleUserRecommender, DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel recommendationModel, Set<Item> candidateItems) throws UserNotFound {

        return users.parallelStream()
                .map(idUser -> new SingleUserRecommendationTask(
                                singleUserRecommender,
                                datasetLoader,
                                recommendationModel.getRecommendationModel(),
                                idUser,
                                candidateItems))
                .map(new SingleUserRecommendationTaskExecutor())
                .collect(Collectors.toMap(
                                recommendationsToUser -> recommendationsToUser.getUser(),
                                recommendationsToUser -> recommendationsToUser.getRecommendations()
                        ));
    }
}
