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
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.generated.modifieddatasets.pseudouser.PseudoUserDatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementa un sistema de recomendación a grupos que agrega las valoraciones de cada individuo para formar un perfil
 * asociado al grupo. Una vez obtenido este perfil, calcula las recomendaciones al grupo como si éste se tratara de un
 * usuario individual.
 *
 * La técnica utilizada para la agregación de preferencias es calcular la media de las valoraciones de los usuarios en
 * cada producto
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version Unknown date.
 * @version 12-Enero-2014
 */
public class AggregationOfIndividualRatings
        extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupModelWithExplanation<GroupModelPseudoUser, Object>> {

    private static final long serialVersionUID = 1L;
    /**
     * "Especifica el sistema de recomendación single user que se extiende para ser usado en recomendación a grupos.
     */
    public static final Parameter SINGLE_USER_RECOMMENDER = new Parameter(
            "SINGLE_USER_RECOMMENDER",
            new RecommenderSystemParameterRestriction(new KnnMemoryBasedCFRS(), RecommenderSystem.class),
            "Especifica el sistema de recomendación single user que se extiende "
            + "para ser usaso en recomendación a grupos.");
    /**
     * Especifica la técnica de agregación para agregar los ratings de los usuarios y formar el perfil del grupo.
     */
    public static final Parameter AGGREGATION_OPERATOR = new Parameter(
            "AGGREGATION_OPERATOR",
            new ParameterOwnerRestriction(AggregationOperator.class, new Mean()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo.");
    private AggregationOperator oldAggregationOperator = new Mean();

    public AggregationOfIndividualRatings() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(AGGREGATION_OPERATOR);

        addParammeterListener(() -> {
            AggregationOperator newAggregationOperator = (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = "AOI_Ratings"
                    + "_" + oldAggregationOperator.getAlias();

            String newAliasNewParameters
                    = "AOI_Ratings"
                    + "_" + newAggregationOperator.getAlias();

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldAggregationOperator = newAggregationOperator;
                setAlias(newAliasNewParameters);
            }
        });

        setAlias("AOI_Ratings_" + oldAggregationOperator.getAlias());
    }

    public AggregationOfIndividualRatings(RecommenderSystem singleUserRecommender) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
    }

    public AggregationOfIndividualRatings(
            RecommenderSystem singleUserRecommender,
            AggregationOperator aggregationOperator) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(AGGREGATION_OPERATOR, aggregationOperator);
    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        RecommendationModelBuildingProgressListener buildListener = this::fireBuildingProgressChangedEvent;
        getSingleUserRecommender().addRecommendationModelBuildingProgressListener(buildListener);
        Object build = getSingleUserRecommender().buildRecommendationModel(datasetLoader);
        getSingleUserRecommender().removeRecommendationModelBuildingProgressListener(buildListener);
        return new SingleRecommendationModel(build);
    }

    @Override
    public <RatingType extends Rating> GroupModelWithExplanation<GroupModelPseudoUser, Object> buildGroupModel(
            DatasetLoader<RatingType> datasetLoader,
            SingleRecommendationModel recommendationModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        GroupModelWithExplanation<GroupModelPseudoUser, Object> groupModelWithExplanation;
        AggregationOperator aggregationOperator = getAggregationOperator();
        Map<Item, RatingType> groupProfile = getGroupProfile(datasetLoader, aggregationOperator, groupOfUsers);

        groupModelWithExplanation = new GroupModelWithExplanation<>(new GroupModelPseudoUser(groupOfUsers, groupProfile), "No explanantion");

        return groupModelWithExplanation;
    }

    @Override
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, SingleRecommendationModel recommendationModel, GroupModelWithExplanation<GroupModelPseudoUser, Object> groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        //Recojo los parámetros en variables
        RecommenderSystem recommenderSystem = getSingleUserRecommender();
        Map<Item, RatingType> groupRatings_Number = groupModel.getGroupModel().getRatings();
        Collection<Recommendation> groupRecom = recommendWithGroupRatings(
                datasetLoader,
                recommenderSystem,
                recommendationModel,
                groupRatings_Number,
                candidateItems);

        return new GroupRecommendations(groupOfUsers, groupRecom);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    public AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);
    }

    public RecommenderSystem getSingleUserRecommender() {
        return (RecommenderSystem) getParameterValue(SINGLE_USER_RECOMMENDER);
    }

    public static <RatingType extends Rating> Map<Item, RatingType> getGroupProfile(
            DatasetLoader<RatingType> datasetLoader,
            Function<Collection<RatingType>, Optional<RatingType>> ratingsAggregator,
            GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {
        //Generate groupProfile:
        Map<Long, List<RatingType>> groupRatingsList = new TreeMap<>();

        for (long idUser : groupOfUsers.getIdMembers()) {
            Map<Long, RatingType> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            userRatingsRated.keySet().stream().map((idItem) -> {
                if (!groupRatingsList.containsKey(idItem)) {
                    groupRatingsList.put(idItem, new LinkedList<>());
                }
                return idItem;
            }).forEach((idItem) -> {
                groupRatingsList.get(idItem).add(userRatingsRated.get(idItem));
            });
        }

        //Aggregate profiles
        Map<Item, RatingType> groupRatings = new TreeMap<>();
        groupRatingsList.keySet().stream().forEach((idItem) -> {
            List<RatingType> lista = groupRatingsList.get(idItem);
            Optional<RatingType> groupRating = ratingsAggregator.apply(lista);

            if (groupRating.isPresent()) {
                groupRatings.put(groupRating.get().getItem(), groupRating.get());
            }
        });

        return groupRatings;

    }

    public static <RatingType extends Rating> Map<Item, RatingType> getGroupProfile(
            DatasetLoader<RatingType> datasetLoader,
            AggregationOperator aggregationOperator,
            GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {

        Function<Collection<RatingType>, Optional<RatingType>> ratingsAggregator
                = (ratings) -> {

                    List<Number> ratingsValues = ratings.stream().map(rating -> rating.getRatingValue()).collect(Collectors.toList());

                    double aggregatedValue = aggregationOperator.aggregateValues(ratingsValues);

                    RatingType groupRating = (RatingType) ratings.stream()
                    .findFirst().get()
                    .copyWithRatingValue(aggregatedValue);

                    return Optional.of(groupRating);
                };

        return getGroupProfile(
                datasetLoader,
                ratingsAggregator,
                groupOfUsers);
    }

    public static <RatingType extends Rating> Collection<Recommendation> recommendWithGroupRatings(
            DatasetLoader<RatingType> datasetLoader,
            RecommenderSystem recommenderSystem,
            SingleRecommendationModel RecommendationModel,
            Map<Item, RatingType> groupRatings, Set<Item> candidateItems) throws ItemNotFound, NotEnoughtUserInformation, UserNotFound, CannotLoadContentDataset, CannotLoadRatingsDataset {

        PseudoUserDatasetLoader<RatingType> datasetLoaderWithPseudoUser
                = new PseudoUserDatasetLoader<>(datasetLoader);

        User pseudoUser = datasetLoaderWithPseudoUser.addPseudoUser(groupRatings);

        datasetLoaderWithPseudoUser.freeze();

        Recommendations pseudoUserRecommendation = recommenderSystem.recommendToUser(
                datasetLoaderWithPseudoUser,
                RecommendationModel.getRecommendationModel(),
                pseudoUser,
                candidateItems);

        Collection<Recommendation> groupRecom = pseudoUserRecommendation.getRecommendations();
        return groupRecom;
    }
}
