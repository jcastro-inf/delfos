package delfos.dataset.generated.modifieddatasets.changeratings;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Dataset que sobreescribe ratings existentes en un ratings dataset dado.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 * @param <RatingType>
 */
public class RatingsDatasetOverwrite<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    public static <RatingType extends Rating> RatingsDatasetOverwrite<RatingType> createRatingsDataset(
            RatingsDataset<RatingType> ratingsDataset,
            Map<Integer, Map<Integer, RatingType>> newRatings_byUser) {

        return new RatingsDatasetOverwrite<>(ratingsDataset, copyRatingsMap(newRatings_byUser));
    }

    public static <RatingType extends Rating> RatingsDatasetOverwrite<RatingType> createRatingsDataset(
            RatingsDataset<RatingType> ratingsDataset,
            Collection<RatingType> newRatings) {

        final Map<Integer, Map<Integer, RatingType>> newRatings_byUser = new TreeMap<>();
        for (RatingType rating : newRatings) {
            if (!newRatings_byUser.containsKey(rating.idUser)) {
                newRatings_byUser.put(rating.idUser, new TreeMap<>());
            }

            if (newRatings_byUser.get(rating.idUser).containsKey(rating.idItem)) {
                RatingType duplicatedRating = newRatings_byUser.get(rating.idUser).get(rating.idItem);
                throw new IllegalArgumentException("Duplicated rating specified in the newRatings collection, '" + duplicatedRating + "' and '" + rating + "'");
            }

            newRatings_byUser.get(rating.idUser).put(rating.idItem, rating);
        }
        return new RatingsDatasetOverwrite<>(ratingsDataset, newRatings_byUser);
    }

    private final RatingsDataset<RatingType> originalRatingsDataset;
    private final Map<Integer, Map<Integer, RatingType>> newRatings_byUser;
    private final Map<Integer, Map<Integer, RatingType>> newRatings_byItem;

    private RatingsDatasetOverwrite() {
        this.originalRatingsDataset = null;
        this.newRatings_byUser = null;
        this.newRatings_byItem = null;

    }

    private RatingsDatasetOverwrite(RatingsDataset<RatingType> ratingsDataset,
            Map<Integer, Map<Integer, RatingType>> newRatings_byUser) {

        this.originalRatingsDataset = ratingsDataset;
        this.newRatings_byUser = newRatings_byUser;
        this.newRatings_byItem = changeIndexation(newRatings_byUser);
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        if (newRatings_byUser.containsKey(idUser) && newRatings_byUser.get(idUser).containsKey(idItem)) {
            return newRatings_byUser.get(idUser).get(idItem);
        } else {
            return originalRatingsDataset.getRating(idUser, idItem);
        }
    }

    @Override
    public Set<Integer> allUsers() {
        Set<Integer> allUsers = new TreeSet<>();

        allUsers.addAll(originalRatingsDataset.allUsers());
        allUsers.addAll(newRatings_byUser.keySet());

        return allUsers;
    }

    @Override
    public Set<Integer> allRatedItems() {
        Set<Integer> allRatedItems = new TreeSet<>();

        allRatedItems.addAll(originalRatingsDataset.allRatedItems());
        allRatedItems.addAll(newRatings_byItem.keySet());

        return allRatedItems;
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        return getItemRatingsRated(idItem).keySet();
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        if (newRatings_byUser.containsKey(idUser)) {
            Map<Integer, RatingType> modifiedRatings = new TreeMap<>();

            modifiedRatings.putAll(originalRatingsDataset.getUserRatingsRated(idUser));
            modifiedRatings.putAll(newRatings_byUser.get(idUser));

            return modifiedRatings;

        } else {
            return originalRatingsDataset.getUserRatingsRated(idUser);
        }
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {

        if (newRatings_byItem.containsKey(idItem)) {
            Map<Integer, RatingType> modifiedRatings = new TreeMap<>();

            modifiedRatings.putAll(originalRatingsDataset.getItemRatingsRated(idItem));
            modifiedRatings.putAll(newRatings_byItem.get(idItem));

            return modifiedRatings;

        } else {
            return originalRatingsDataset.getItemRatingsRated(idItem);
        }

    }

    @Override
    public Domain getRatingsDomain() {
        return originalRatingsDataset.getRatingsDomain();
    }

    public static <RatingType extends Rating> Map<Integer, Map<Integer, RatingType>> copyRatingsMap(Map<Integer, Map<Integer, RatingType>> newRatings) {

        Map<Integer, Map<Integer, RatingType>> newRatings_byUser = new TreeMap<>();
        for (int idUser : newRatings.keySet()) {
            for (Map.Entry<Integer, RatingType> entry : newRatings.get(idUser).entrySet()) {
                int idItem = entry.getKey();
                RatingType rating = entry.getValue();

                if (idUser != rating.idUser) {
                    throw new IllegalArgumentException("Rating '" + rating + "' does not match its indexation: idUser '" + idUser + "'");
                }
                if (idItem != rating.idItem) {
                    throw new IllegalArgumentException("Rating '" + rating + "' does not match its indexation: idItem '" + idItem + "'");
                }

                if (!newRatings_byUser.containsKey(rating.idUser)) {
                    newRatings_byUser.put(rating.idUser, new TreeMap<>());
                }

                if (newRatings_byUser.get(rating.idUser).containsKey(rating.idItem)) {
                    RatingType duplicatedRating = newRatings_byUser.get(rating.idUser).get(rating.idItem);
                    throw new IllegalArgumentException("Duplicated rating specified in the newRatings collection, '" + duplicatedRating + "' and '" + rating + "'");
                }
            }
        }
        return newRatings_byUser;
    }

    private Map<Integer, Map<Integer, RatingType>> changeIndexation(Map<Integer, Map<Integer, RatingType>> ratings) {
        Map<Integer, Map<Integer, RatingType>> changedIndexation = new TreeMap<>();

        for (int rowsIndex : ratings.keySet()) {
            for (int columnsIndex : ratings.get(rowsIndex).keySet()) {
                if (!changedIndexation.containsKey(columnsIndex)) {
                    changedIndexation.put(columnsIndex, new TreeMap<>());
                }

                changedIndexation.get(columnsIndex).put(rowsIndex, ratings.get(rowsIndex).get(columnsIndex));
            }
        }

        return changedIndexation;
    }
}
