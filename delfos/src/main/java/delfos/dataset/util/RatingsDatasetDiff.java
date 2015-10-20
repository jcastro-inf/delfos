package delfos.dataset.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import static delfos.dataset.util.DatasetPrinter.printCompactMatrix;
import delfos.dataset.util.ratings.RatingValueModificationMarker;
import delfos.view.console.util.ConsoleColor;

/**
 * Clase para calcular diferencias entre dos dataset de valoraciones.
 *
 * @author Jorge Castro Gallardo
 * @version 18-feb-2015
 */
public class RatingsDatasetDiff {

    private static final String MARK_RATING_DELETED = ConsoleColor.RED + "del" + ConsoleColor.RESET;
    private static final String MARK_RATING_ADDED = ConsoleColor.GREEN + "add" + ConsoleColor.RESET;

    private static final String MARK_RATING_VALUE_MODIFIED_UP = ConsoleColor.CYAN + "+" + ConsoleColor.RESET;
    private static final String MARK_RATING_VALUE_MODIFIED_STAYS = "==";
    private static final String MARK_RATING_VALUE_MODIFIED_DOWN = ConsoleColor.BLUE + "-" + ConsoleColor.RESET;

    public static String printDiff(RatingsDataset<? extends Rating> oldDataset, RatingsDataset<? extends Rating> newDataset) {
        Map<Object, Map<Object, String>> diff_byUser = diff_byUser(oldDataset, newDataset);

        return printCompactMatrix(diff_byUser);
    }

    private static Map<Object, Map<Object, String>> diff_byUser(RatingsDataset<? extends Rating> oldDataset, RatingsDataset<? extends Rating> newDataset) throws RuntimeException {
        Map<Object, Map<Object, String>> diffMatrix = new TreeMap<>();
        TreeSet<Integer> unionOfUsers = new TreeSet<>();
        unionOfUsers.addAll(oldDataset.allUsers());
        unionOfUsers.addAll(newDataset.allUsers());
        //Cuales hay en oldRatingsDataset que no estén en newRatingsDataset:
        for (int idUser : unionOfUsers) {

            try {
                TreeMap<Object, String> diffThisUser = new TreeMap<>();

                diffThisUser.putAll(userDeleted(oldDataset, newDataset, idUser));
                diffThisUser.putAll(userAdded(oldDataset, newDataset, idUser));

                diffThisUser.putAll(userRatingsDeleted(oldDataset, newDataset, idUser));
                diffThisUser.putAll(userRatingsAdded(oldDataset, newDataset, idUser));

                diffThisUser.putAll(userRatingsModified(oldDataset, newDataset, idUser));

                diffMatrix.put(idUser, diffThisUser);
            } catch (UserNotFound ex) {
                ERROR_CODES.UNDEFINED_ERROR.exit(new IllegalStateException("This error should never happen if the code is right and the datasets do not change over time."));
            }

        }
        return diffMatrix;
    }

    public static String printDiffHistogram(RatingsDataset<? extends Rating> oldDataset, RatingsDataset<? extends Rating> newDataset) {
        Map<String, Integer> diffHistogram = diffHistogram(oldDataset, newDataset);

        StringBuilder s = new StringBuilder();

        s.append(MARK_RATING_DELETED).append("\t").append(diffHistogram.get(MARK_RATING_DELETED)).append("\n");

        for (String modificationMarker : RatingValueModificationMarker.generateDefaultMarkers(MARK_RATING_VALUE_MODIFIED_UP, MARK_RATING_VALUE_MODIFIED_STAYS, MARK_RATING_VALUE_MODIFIED_DOWN, MARK_RATING_VALUE_MODIFIED_RANGE_WIDTH)) {
            s.append(modificationMarker).append("\t").append(diffHistogram.get(modificationMarker)).append("\n");
        }
        s.append(MARK_RATING_ADDED).append("\t").append(diffHistogram.get(MARK_RATING_DELETED)).append("\n");

        return s.toString();
    }

    public static Map<String, Integer> diffHistogram(RatingsDataset<? extends Rating> oldDataset, RatingsDataset<? extends Rating> newDataset) {
        Map<String, Integer> diffHistogram = new TreeMap<>();

        diffHistogram.put(MARK_RATING_ADDED, 0);
        diffHistogram.put(MARK_RATING_DELETED, 0);

        for (String diffMarkModified : RatingValueModificationMarker.generateDefaultMarkers(MARK_RATING_VALUE_MODIFIED_UP, MARK_RATING_VALUE_MODIFIED_STAYS, MARK_RATING_VALUE_MODIFIED_DOWN, MARK_RATING_VALUE_MODIFIED_RANGE_WIDTH)) {
            diffHistogram.put(diffMarkModified, 0);
        }

        Map<Object, Map<Object, String>> diff_byUser = diff_byUser(oldDataset, newDataset);

        for (Object user : diff_byUser.keySet()) {
            for (Object item : diff_byUser.get(user).keySet()) {
                String diffMarkerOfThisRating = diff_byUser.get(user).get(item);
                diffHistogram.replace(diffMarkerOfThisRating, diffHistogram.get(diffMarkerOfThisRating) + 1);
            }
        }

        return diffHistogram;
    }

    private static Map<Object, String> userDeleted(RatingsDataset<? extends Rating> oldRatingsDataset, RatingsDataset<? extends Rating> newRatingsDataset, int idUser) throws UserNotFound {

        if (oldRatingsDataset.allUsers().contains(idUser) && !newRatingsDataset.allUsers().contains(idUser)) {
            TreeMap<Object, String> diffThisUser = new TreeMap<>();

            for (int idItemRatedInOneButNotInTwo : oldRatingsDataset.getUserRated(idUser)) {
                diffThisUser.put(idItemRatedInOneButNotInTwo, MARK_RATING_DELETED);
            }
            return diffThisUser;
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    private static Map<? extends Object, ? extends String> userAdded(RatingsDataset<? extends Rating> oldRatingsDataset, RatingsDataset<? extends Rating> newRatingsDataset, int idUser) throws UserNotFound {
        if (!oldRatingsDataset.allUsers().contains(idUser) && newRatingsDataset.allUsers().contains(idUser)) {
            TreeMap<Object, String> diffThisUser = new TreeMap<>();
            for (int idItemRatedInOneButNotInTwo : newRatingsDataset.getUserRated(idUser)) {
                diffThisUser.put(idItemRatedInOneButNotInTwo, MARK_RATING_ADDED);
            }
            return diffThisUser;
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    private static Map<? extends Object, ? extends String> userRatingsDeleted(RatingsDataset<? extends Rating> oldRatingsDataset, RatingsDataset<? extends Rating> newRatingsDataset, int idUser) throws UserNotFound {
        if (oldRatingsDataset.allUsers().contains(idUser) && newRatingsDataset.allUsers().contains(idUser)) {

            TreeMap<Object, String> diffThisUser = new TreeMap<>();

            TreeSet<Integer> ratingsDeleted = new TreeSet<>();

            ratingsDeleted.addAll(oldRatingsDataset.getUserRated(idUser));
            ratingsDeleted.removeAll(newRatingsDataset.getUserRated(idUser));

            ratingsDeleted.stream().forEach((idItemRatingsDeleted) -> {
                diffThisUser.put(idItemRatingsDeleted, MARK_RATING_DELETED);
            });

            return diffThisUser;

        } else {
            return Collections.EMPTY_MAP;
        }
    }

    private static Map<? extends Object, ? extends String> userRatingsAdded(RatingsDataset<? extends Rating> oldRatingsDataset, RatingsDataset<? extends Rating> newRatingsDataset, int idUser) throws UserNotFound {
        if (oldRatingsDataset.allUsers().contains(idUser) && newRatingsDataset.allUsers().contains(idUser)) {

            TreeMap<Object, String> diffThisUser = new TreeMap<>();

            TreeSet<Integer> itemsAdded = new TreeSet<>();

            itemsAdded.addAll(newRatingsDataset.getUserRated(idUser));
            itemsAdded.removeAll(oldRatingsDataset.getUserRated(idUser));

            itemsAdded.stream().forEach((idItemAdded) -> {
                diffThisUser.put(idItemAdded, MARK_RATING_ADDED);
            });

            return diffThisUser;

        } else {
            return Collections.EMPTY_MAP;
        }
    }

    private static Map<? extends Object, ? extends String> userRatingsModified(RatingsDataset<? extends Rating> oldRatingsDataset, RatingsDataset<? extends Rating> newRatingsDataset, int idUser) throws UserNotFound {
        if (oldRatingsDataset.allUsers().contains(idUser) && newRatingsDataset.allUsers().contains(idUser)) {

            TreeMap<Object, String> diffThisUser = new TreeMap<>();

            RatingValueModificationMarker ratingValueModificationMarkers = new RatingValueModificationMarker(oldRatingsDataset.getRatingsDomain(),
                    RatingValueModificationMarker.generateDefaultMarkers(MARK_RATING_VALUE_MODIFIED_UP, MARK_RATING_VALUE_MODIFIED_STAYS, MARK_RATING_VALUE_MODIFIED_DOWN, MARK_RATING_VALUE_MODIFIED_RANGE_WIDTH));

            Map<Integer, ? extends Rating> userRatingsRated1 = oldRatingsDataset.getUserRatingsRated(idUser);
            Map<Integer, ? extends Rating> userRatingsRated2 = newRatingsDataset.getUserRatingsRated(idUser);

            Set<Integer> userRatedIntersection = new TreeSet<>();

            userRatedIntersection.addAll(userRatingsRated1.keySet());
            userRatedIntersection.retainAll(userRatingsRated2.keySet());

            userRatedIntersection.stream().forEach((idItem) -> {
                double oldValue = userRatingsRated1.get(idItem).getRatingValue().doubleValue();
                double newValue = userRatingsRated2.get(idItem).getRatingValue().doubleValue();

                diffThisUser.put(idItem, ratingValueModificationMarkers.getRatingModificationMarker(oldValue, newValue));
            });
            return diffThisUser;
        } else {
            return Collections.EMPTY_MAP;
        }
    }
    private static final int MARK_RATING_VALUE_MODIFIED_RANGE_WIDTH = 4;

    public static boolean equalsRatingsValues(RatingsDataset<? extends Rating> r1, RatingsDataset<? extends Rating> r2) {

        if (r1.getNumRatings() != r2.getNumRatings()) {
            return false;
        }

        if (!r1.allUsers().equals(r2.allUsers())) {
            return false;
        }

        if (!r1.allRatedItems().equals(r2.allRatedItems())) {
            return false;
        }

        for (Integer user : r1.allUsers()) {
            try {
                Map<Integer, ? extends Rating> userRatingsRated_r1 = r1.getUserRatingsRated(user);

                try {
                    Map<Integer, ? extends Rating> userRatingsRated_r2 = r2.getUserRatingsRated(user);
                    if (differentUserRatings(userRatingsRated_r1, userRatingsRated_r2)) {
                        return false;
                    }
                } catch (UserNotFound ex) {
                    if (!userRatingsRated_r1.isEmpty()) {
                        return false;
                    }
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
            }
        }

        return true;
    }

    private static boolean differentUserRatings(Map<Integer, ? extends Rating> userRatingsRated_r1, Map<Integer, ? extends Rating> userRatingsRated_r2) {
        if (!userRatingsRated_r1.keySet().equals(userRatingsRated_r2.keySet())) {
            return true;
        }
        for (Integer idItem : userRatingsRated_r1.keySet()) {
            Rating rating_r1 = userRatingsRated_r1.get(idItem);
            Rating rating_r2 = userRatingsRated_r2.get(idItem);
            double value_r1 = rating_r1.getRatingValue().doubleValue();
            double value_r2 = rating_r2.getRatingValue().doubleValue();
            if (value_r1 != value_r2) {
                return true;
            }
        }
        return false;
    }

}
