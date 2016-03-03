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
package delfos.group.grs.hesitant;

import delfos.common.aggregationoperators.Mean;
import delfos.common.decimalnumbers.NumberCompare;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.generated.modifieddatasets.pseudouser.PseudoUserDatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationWithNeighbors;
import delfos.utils.hesitant.HesitantValuation;
import delfos.utils.hesitant.similarity.HesitantPearson;
import delfos.utils.hesitant.similarity.HesitantSimilarity;
import delfos.utils.hesitant.similarity.factory.HesitantSimilarityFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class HesitantKnnGroupUser
        extends GroupRecommenderSystemAdapter<Object, HesitantValuation> {

    /**
     * Parámetro para indicar la medida de similitud que el sistema de
     * recomendación utiliza para el cálculo de los vecinos más cercanos. Si no
     * se modifica, su valor por defecto es la suma ponderada
     * ({@link CosineCoefficient})
     */
    public static final Parameter HESITANT_SIMILARITY_MEASURE = new Parameter(
            "SIMILARITY_MEASURE",
            new ObjectParameter(
                    HesitantSimilarityFactory.getAll(),
                    HesitantSimilarityFactory.getHesitantSimilarity(HesitantPearson.class.getSimpleName())
            )
    );

    public static final Parameter NEIGHBORHOOD_SIZE = delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE;
    public static final Parameter PREDICTION_TECHNIQUE = delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.PREDICTION_TECHNIQUE;

    public static final Parameter DELETE_REPEATED = new Parameter("Delete_repeated", new BooleanParameter(Boolean.FALSE));

    public HesitantKnnGroupUser() {
        super();

        addParameter(NEIGHBORHOOD_SIZE);
        addParameter(HESITANT_SIMILARITY_MEASURE);
        addParameter(PREDICTION_TECHNIQUE);
        addParameter(DELETE_REPEATED);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public Object buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        return 1;
    }

    @Override
    public <RatingType extends Rating> HesitantValuation buildGroupModel(
            DatasetLoader<RatingType> datasetLoader,
            Object RecommendationModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        HesitantValuation<Item, Double> hesitantProfile = getHesitantProfile(datasetLoader, groupOfUsers.getMembers());

        if (isDeleteRepeatedOn()) {
            Comparator<Double> comparator = (Double o1, Double o2) -> NumberCompare.compare(o1, o2);
            hesitantProfile = hesitantProfile.deleteRepeated(comparator);
        }
        return hesitantProfile;
    }

    @Override
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, Object RecommendationModel, HesitantValuation groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        try {
            List<Neighbor> neighbors;
            RatingsDataset<RatingType> ratingsDataset = datasetLoader.getRatingsDataset();
            neighbors = getNeighbors(datasetLoader, groupModel, groupOfUsers);

            int neighborhoodSize = (int) getParameterValue(NEIGHBORHOOD_SIZE);

            PredictionTechnique predictionTechnique = (PredictionTechnique) getParameterValue(PREDICTION_TECHNIQUE);

            Map<Item, RatingType> groupRatings
                    = AggregationOfIndividualRatings.getGroupProfile(datasetLoader, new Mean(), groupOfUsers);

            PseudoUserDatasetLoader<RatingType> pseudoUserDatasetLoader
                    = new PseudoUserDatasetLoader<>(datasetLoader
                    );

            User pseudoUser = pseudoUserDatasetLoader.addPseudoUser(groupRatings);

            Collection<Recommendation> ret = KnnMemoryBasedNWR.recommendWithNeighbors(
                    pseudoUserDatasetLoader,
                    pseudoUser.getId(),
                    neighbors,
                    neighborhoodSize, candidateItems.stream().map(item -> item.getId()).collect(Collectors.toSet()),
                    predictionTechnique);

            Collection<Recommendation> retWithNeighbors = ret.stream()
                    .map(recommendation -> new RecommendationWithNeighbors(
                                    recommendation.getItem(),
                                    recommendation.getPreference(),
                                    neighbors))
                    .collect(Collectors.toList());

            return new GroupRecommendations(groupOfUsers, retWithNeighbors);
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private List<Neighbor> getNeighbors(
            DatasetLoader<? extends Rating> datasetLoader,
            HesitantValuation groupModel,
            GroupOfUsers groupOfUsers) {

        HesitantSimilarity similarity = (HesitantSimilarity) getParameterValue(HESITANT_SIMILARITY_MEASURE);

        Stream<Neighbor> neighborsStream = datasetLoader.getUsersDataset()
                .stream()
                .map(idNeighbor -> new HesitantKnnNeighborSimilarityTask(
                                datasetLoader, groupOfUsers, groupModel, idNeighbor, similarity))
                .map(new HesitantKnnNeighborSimilarityFunction());

        List<Neighbor> neighbors = neighborsStream
                .sorted(Neighbor.BY_SIMILARITY_DESC)
                .collect(Collectors.toList());

        return neighbors;
    }

    public static HesitantValuation<Item, Double> getHesitantProfile(DatasetLoader<? extends Rating> datasetLoader, Collection<User> users) {
        Collection<HesitantValuation.HesitantSingleValuation<Item, Double>> valuations = new ArrayList<>();

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        datasetLoader.getContentDataset();
        for (User user : users) {
            Collection<? extends Rating> ratings = ratingsDataset.getUserRatingsRated(user.getId()).values();
            for (Rating rating : ratings) {
                Item item = rating.getItem();
                double ratingValue = rating.getRatingValue().doubleValue();
                HesitantValuation.HesitantSingleValuation<Item, Double> valuation
                        = new HesitantValuation.HesitantSingleValuation<>(item, ratingValue);
                valuations.add(valuation);
            }
        }

        HesitantValuation<Item, Double> groupProfileHesitant = new HesitantValuation<>(valuations);
        return groupProfileHesitant;
    }

    private boolean isDeleteRepeatedOn() {
        return (Boolean) getParameterValue(DELETE_REPEATED);
    }
}
