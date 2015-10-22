package delfos.dataset.util;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Clase para transformar mapas de valoraciones
 *
 * @author Jorge Castro Gallardo
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

}
