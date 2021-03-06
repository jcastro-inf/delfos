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
package delfos.dataset.util;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Class with utility methods to manage different indexing of ratings.
 * <p>
 * <p>
 * It includes transformations such as converting user indexed maps to item indexed
 * <p>
 * Extraction of submatrices from datasets.
 * <p>
 * Conversion from ratings to recommendations and vice-versa.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DatasetUtilities {

    public static Map<Long, Map<Long, Rating>> getMapOfMaps_Rating(Map<Long, Map<Long, Number>> ratings) {
        Map<Long, Map<Long, Rating>> ret = new TreeMap<>();
        for (Map.Entry<Long, Map<Long, Number>> userRatingsEntry : ratings.entrySet()) {
            long idUser = userRatingsEntry.getKey();
            Map<Long, Number> userRatings = userRatingsEntry.getValue();
            ret.put(idUser, getUserMap_Rating(idUser, userRatings));
        }
        return ret;
    }

    public static Map<Long, Rating> getUserMap_Rating(long idUser, Map<Long, Number> userRatings) {
        Map<Long, Rating> ret = new TreeMap<>();
        for (Map.Entry<Long, ? extends Number> userEntry : userRatings.entrySet()) {
            long idItem = userEntry.getKey();
            Number rating = userEntry.getValue();
            ret.put(idItem, new Rating(idUser, idItem, rating));
        }
        return ret;
    }

    public static Map<Long, Number> getUserMap_Number(long idUser, Map<Long, Rating> userRatings) {
        Map<Long, Number> ret = new TreeMap<>();
        for (Map.Entry<Long, Rating> userEntry : userRatings.entrySet()) {
            long idItem = userEntry.getKey();
            Rating rating = userEntry.getValue();
            ret.put(idItem, rating.getRatingValue());
        }
        return ret;
    }

    public static Map<Long, Map<Long, Number>> getMapOfMaps_Number(Map<Long, Map<Long, Rating>> ratings) {
        Map<Long, Map<Long, Number>> ret = new TreeMap<>();
        for (Map.Entry<Long, Map<Long, Rating>> userRatingsEntry : ratings.entrySet()) {
            Long idUser = userRatingsEntry.getKey();
            Map<Long, Rating> userRatings = userRatingsEntry.getValue();
            ret.put(idUser, getUserMap_Number(idUser, userRatings));
        }
        return ret;
    }

    public static Map<Long, Map<Long, Number>> transformIndexedByUsersToIndexedByItems_Map(
            Map<Long, Map<Long, Number>> ratings_byUser) {

        Map<Long, Map<Long, Number>> ratings_byItem = new TreeMap<>();

        for (Long idUser : ratings_byUser.keySet()) {
            Map<Long, Number> userRatings = ratings_byUser.get(idUser);

            for (Map.Entry<Long, Number> entry : userRatings.entrySet()) {
                Long idItem = entry.getKey();
                Number rating = entry.getValue();

                if (!ratings_byItem.containsKey(idItem)) {
                    ratings_byItem.put(idItem, new TreeMap<>());
                }

                ratings_byItem.get(idItem).put(idUser, rating);
            }
        }

        return ratings_byItem;
    }

    public static Map<Long, Map<Long, Number>> transformIndexedByItemToIndexedByUser_Map(Map<Long, Map<Long, Number>> ratingsByItem) {
        Map<Long, Map<Long, Number>> ratingsByUser = new TreeMap<>();

        for (Long idItem : ratingsByItem.keySet()) {
            Map<Long, Number> itemRatings = ratingsByItem.get(idItem);

            for (Map.Entry<Long, Number> entry : itemRatings.entrySet()) {
                Long idUser = entry.getKey();
                Number rating = entry.getValue();

                if (!ratingsByUser.containsKey(idUser)) {
                    ratingsByUser.put(idUser, new TreeMap<>());
                }

                ratingsByUser.get(idUser).put(idItem, rating);
            }
        }

        return ratingsByUser;
    }

    public static Map<Long, Map<Long, Number>> getMembersRatings_byItem(GroupOfUsers groupOfUsers, DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, UserNotFound {
        Map<Long, Map<Long, Number>> membersRatings_byUser = getMembersRatings_byUser(groupOfUsers, datasetLoader);
        return transformIndexedByUsersToIndexedByItems_Map(membersRatings_byUser);
    }

    public static Map<Long, Map<Long, Number>> getMembersRatings_byUser(GroupOfUsers groupOfUsers, DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, UserNotFound {
        Map<Long, Map<Long, Number>> membersRatings = new TreeMap<>();
        for (Long idUser : groupOfUsers.getIdMembers()) {
            Map<Long, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            membersRatings.put(idUser, new TreeMap<>());
            userRatingsRated.keySet().stream().forEach((Long idItem) -> {
                Number rating = userRatingsRated.get(idItem).getRatingValue();
                membersRatings.get(idUser).put(idItem, rating);
            });
        }
        return membersRatings;
    }

    protected static List<Recommendation> convertRatingsMapToRecommendationList(Map<Long, Number> groupAggregatedProfile) {
        List<Recommendation> recommendations = new ArrayList<>(groupAggregatedProfile.size());
        for (Map.Entry<Long, Number> entry : groupAggregatedProfile.entrySet()) {
            Long idItem = entry.getKey();
            Number preference = entry.getValue();
            recommendations.add(new Recommendation(idItem, preference));
        }
        return recommendations;
    }

    public static Map<User, Map<Item, Recommendation>> convertToMapOfRecommendationsByMember(Map<User, Collection<Recommendation>> recommendationsByMember) {
        Map<User, Map<Item, Recommendation>> mapOfMapOfRecommendationsByMember = new TreeMap<>();

        for (Map.Entry<User, Collection<Recommendation>> entry : recommendationsByMember.entrySet()) {
            User member = entry.getKey();
            Collection<Recommendation> recommendations = entry.getValue();

            Map<Item, Recommendation> recommendationsByItem = getMapOfRecommendationsByItem(recommendations);

            mapOfMapOfRecommendationsByMember.put(member, recommendationsByItem);
        }
        return mapOfMapOfRecommendationsByMember;
    }

    public static Map<Item, Recommendation> convertToMapOfRecommendations(Recommendations recommendations) {

        return recommendations.getRecommendations().parallelStream().collect(Collectors.toMap(
                recommendation -> recommendation.getItem(),
                recommendation -> recommendation));
    }

    private static Map<Item, Recommendation> getMapOfRecommendationsByItem(Collection<Recommendation> recommendations) {

        TreeMap<Item, Recommendation> mapOfRecommendationsByItem = new TreeMap<>();

        for (Recommendation recommendation : recommendations) {

            mapOfRecommendationsByItem.put(recommendation.getItem(), recommendation);

        }

        return mapOfRecommendationsByItem;
    }

    public static <IndexA, IndexB, Element> Map<IndexB, Map<IndexA, Element>> transpose(Map<IndexA, Map<IndexB, Element>> convertToMapOfRecommendationsByMember) {
        Map<IndexB, Map<IndexA, Element>> mapIndexedByBThenA = new TreeMap<>();

        for (IndexA indexA : convertToMapOfRecommendationsByMember.keySet()) {
            Map<IndexB, Element> mapIndexedByB = convertToMapOfRecommendationsByMember.get(indexA);

            for (IndexB indexB : mapIndexedByB.keySet()) {
                Element element = mapIndexedByB.get(indexB);
                if (!mapIndexedByBThenA.containsKey(indexB)) {
                    mapIndexedByBThenA.put(indexB, new TreeMap<>());
                }
                mapIndexedByBThenA.get(indexB).put(indexA, element);
            }
        }
        return mapIndexedByBThenA;
    }

    public static Map<User, Map<Item, Recommendation>> convertToMapOfRecommendationsByMember(
            Collection<RecommendationsToUser> recommendationsForConsensusByMember) {

        return recommendationsForConsensusByMember.parallelStream().collect(Collectors.toMap(
                memberRecommendations -> memberRecommendations.getUser(),
                memberRecommendations -> memberRecommendations
                .getRecommendations().stream()
                .collect(Collectors.toMap(
                        recommendation -> recommendation.getItem(),
                        recommendation -> recommendation)
                )
        ));
    }

    public static <RatingType extends Rating> Map<User, Map<Item, RatingType>> getRatingsByUserAndItem(
            DatasetLoader<RatingType> datasetLoader,
            Collection<User> users) {

        Map<User, Map<Item, RatingType>> ret = users.parallelStream().collect(Collectors.toMap(user -> user, user -> {
            Map<Long, RatingType> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

            Map<Item, RatingType> userRatings = userRatingsRated.values().parallelStream().collect(Collectors.toMap(
                    rating -> rating.getItem(),
                    rating -> rating));

            return userRatings;
        }));

        return ret;
    }

}
