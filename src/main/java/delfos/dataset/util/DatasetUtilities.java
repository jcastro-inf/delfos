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
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Clase para transformar mapas de valoraciones
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 25-Agosto-2014
 */
public class DatasetUtilities {

    public static Map<Integer, Map<Integer, Rating>> getMapOfMaps_Rating(Map<Integer, Map<Integer, Number>> ratings) {
        Map<Integer, Map<Integer, Rating>> ret = new TreeMap<>();
        for (Map.Entry<Integer, Map<Integer, Number>> userRatingsEntry : ratings.entrySet()) {
            int idUser = userRatingsEntry.getKey();
            Map<Integer, Number> userRatings = userRatingsEntry.getValue();
            ret.put(idUser, getUserMap_Rating(idUser, userRatings));
        }
        return ret;
    }

    public static Map<Integer, Rating> getUserMap_Rating(int idUser, Map<Integer, Number> userRatings) {
        Map<Integer, Rating> ret = new TreeMap<>();
        for (Map.Entry<Integer, ? extends Number> userEntry : userRatings.entrySet()) {
            int idItem = userEntry.getKey();
            Number rating = userEntry.getValue();
            ret.put(idItem, new Rating(idUser, idItem, rating));
        }
        return ret;
    }

    public static Map<Integer, Number> getUserMap_Number(int idUser, Map<Integer, Rating> userRatings) {
        Map<Integer, Number> ret = new TreeMap<>();
        for (Map.Entry<Integer, Rating> userEntry : userRatings.entrySet()) {
            int idItem = userEntry.getKey();
            Rating rating = userEntry.getValue();
            ret.put(idItem, rating.getRatingValue());
        }
        return ret;
    }

    public static Map<Integer, Map<Integer, Number>> getMapOfMaps_Number(Map<Integer, Map<Integer, Rating>> ratings) {
        Map<Integer, Map<Integer, Number>> ret = new TreeMap<>();
        for (Map.Entry<Integer, Map<Integer, Rating>> userRatingsEntry : ratings.entrySet()) {
            int idUser = userRatingsEntry.getKey();
            Map<Integer, Rating> userRatings = userRatingsEntry.getValue();
            ret.put(idUser, getUserMap_Number(idUser, userRatings));
        }
        return ret;
    }

    public static Map<Integer, Map<Integer, Number>> transformIndexedByUsersToIndexedByItems_Map(
            Map<Integer, Map<Integer, Number>> ratings_byUser) {

        Map<Integer, Map<Integer, Number>> ratings_byItem = new TreeMap<>();

        for (int idUser : ratings_byUser.keySet()) {
            Map<Integer, Number> userRatings = ratings_byUser.get(idUser);

            for (Map.Entry<Integer, Number> entry : userRatings.entrySet()) {
                int idItem = entry.getKey();
                Number rating = entry.getValue();

                if (!ratings_byItem.containsKey(idItem)) {
                    ratings_byItem.put(idItem, new TreeMap<>());
                }

                ratings_byItem.get(idItem).put(idUser, rating);
            }
        }

        return ratings_byItem;
    }

    public static Map<Integer, Map<Integer, Number>> transformIndexedByItemToIndexedByUser_Map(Map<Integer, Map<Integer, Number>> ratingsByItem) {
        Map<Integer, Map<Integer, Number>> ratingsByUser = new TreeMap<>();

        for (int idItem : ratingsByItem.keySet()) {
            Map<Integer, Number> itemRatings = ratingsByItem.get(idItem);

            for (Map.Entry<Integer, Number> entry : itemRatings.entrySet()) {
                int idUser = entry.getKey();
                Number rating = entry.getValue();

                if (!ratingsByUser.containsKey(idUser)) {
                    ratingsByUser.put(idUser, new TreeMap<>());
                }

                ratingsByUser.get(idUser).put(idItem, rating);
            }
        }

        return ratingsByUser;
    }

    public static Map<Integer, Map<Integer, Number>> getMembersRatings_byItem(GroupOfUsers groupOfUsers, DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, UserNotFound {
        Map<Integer, Map<Integer, Number>> membersRatings_byUser = getMembersRatings_byUser(groupOfUsers, datasetLoader);
        return transformIndexedByUsersToIndexedByItems_Map(membersRatings_byUser);
    }

    public static Map<Integer, Map<Integer, Number>> getMembersRatings_byUser(GroupOfUsers groupOfUsers, DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, UserNotFound {
        Map<Integer, Map<Integer, Number>> membersRatings = new TreeMap<>();
        for (int idUser : groupOfUsers.getIdMembers()) {
            Map<Integer, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            membersRatings.put(idUser, new TreeMap<>());
            userRatingsRated.keySet().stream().forEach((Integer idItem) -> {
                Number rating = userRatingsRated.get(idItem).getRatingValue();
                membersRatings.get(idUser).put(idItem, rating);
            });
        }
        return membersRatings;
    }

    protected static List<Recommendation> convertRatingsMapToRecommendationList(Map<Integer, Number> groupAggregatedProfile) {
        List<Recommendation> recommendations = new ArrayList<>(groupAggregatedProfile.size());
        for (Map.Entry<Integer, Number> entry : groupAggregatedProfile.entrySet()) {
            int idItem = entry.getKey();
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

    public static Map convertToMemberRatings(Map<Integer, Collection<Recommendation>> recommendationsLists_byMember) {
        Map<Integer, List<Number>> membersRatingsPrediction_byItem = new TreeMap<>();
        for (int idUser : recommendationsLists_byMember.keySet()) {
            for (Recommendation recommendation : recommendationsLists_byMember.get(idUser)) {
                int idItem = recommendation.getIdItem();
                Number prediction = recommendation.getPreference();
                if (!membersRatingsPrediction_byItem.containsKey(idItem)) {
                    membersRatingsPrediction_byItem.put(idItem, new ArrayList<>());
                }
                membersRatingsPrediction_byItem.get(idItem).add(prediction);
            }
        }
        return membersRatingsPrediction_byItem;
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
        )
        );
    }

    public static <RatingType extends Rating> Map<User, Map<Item, RatingType>> getRatingsByUserAndItem(
            DatasetLoader<RatingType> datasetLoader,
            Collection<User> users) {

        Map<User, Map<Item, RatingType>> ret = users.parallelStream().collect(Collectors.toMap(user -> user, user -> {
            Map<Integer, RatingType> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

            userRatingsRated.values().parallelStream().collect(Collectors.toMap(
                    rating -> rating.getItem(),
                    rating -> rating));

            return null;
        }
        )
        );

        return ret;
    }

}
