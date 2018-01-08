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
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Dataset que sobreescribe ratings existentes en un ratings dataset dado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RatingType>
 */
public class RatingsDatasetOverwrite<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    public static <RatingType extends Rating> RatingsDatasetOverwrite<RatingType> createRatingsDataset(
            RatingsDataset<RatingType> ratingsDataset,
            Map<Long, Map<Long, RatingType>> newRatings_byUser) {

        return new RatingsDatasetOverwrite<>(ratingsDataset, copyRatingsMap(newRatings_byUser));
    }

    public static <RatingType extends Rating> RatingsDatasetOverwrite<RatingType> createRatingsDataset(
            RatingsDataset<RatingType> ratingsDataset,
            Collection<RatingType> newRatings) {

        final Map<Long, Map<Long, RatingType>> newRatings_byUser = new TreeMap<>();
        for (RatingType rating : newRatings) {
            if (!newRatings_byUser.containsKey(rating.getIdUser())) {
                newRatings_byUser.put(rating.getIdUser(), new TreeMap<>());
            }

            if (newRatings_byUser.get(rating.getIdUser()).containsKey(rating.getIdItem())) {
                RatingType duplicatedRating = newRatings_byUser.get(rating.getIdUser()).get(rating.getIdItem());
                throw new IllegalArgumentException("Duplicated rating specified in the newRatings collection, '" + duplicatedRating + "' and '" + rating + "'");
            }

            newRatings_byUser.get(rating.getIdUser()).put(rating.getIdItem(), rating);
        }
        return new RatingsDatasetOverwrite<>(ratingsDataset, newRatings_byUser);
    }

    private final RatingsDataset<RatingType> originalRatingsDataset;
    private final Map<Long, Map<Long, RatingType>> newRatings_byUser;
    private final Map<Long, Map<Long, RatingType>> newRatings_byItem;

    private RatingsDatasetOverwrite() {
        this.originalRatingsDataset = null;
        this.newRatings_byUser = null;
        this.newRatings_byItem = null;

    }

    private RatingsDatasetOverwrite(RatingsDataset<RatingType> ratingsDataset,
            Map<Long, Map<Long, RatingType>> newRatings_byUser) {

        this.originalRatingsDataset = ratingsDataset;
        this.newRatings_byUser = newRatings_byUser;
        this.newRatings_byItem = changeIndexation(newRatings_byUser);
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        if (newRatings_byUser.containsKey(idUser) && newRatings_byUser.get(idUser).containsKey(idItem)) {
            return newRatings_byUser.get(idUser).get(idItem);
        } else {
            return originalRatingsDataset.getRating(idUser, idItem);
        }
    }

    @Override
    public Set<Long> allUsers() {
        Set<Long> allUsers = new TreeSet<>();

        allUsers.addAll(originalRatingsDataset.allUsers());
        allUsers.addAll(newRatings_byUser.keySet());

        return allUsers;
    }

    @Override
    public Set<Long> allRatedItems() {
        Set<Long> allRatedItems = new TreeSet<>();

        allRatedItems.addAll(originalRatingsDataset.allRatedItems());
        allRatedItems.addAll(newRatings_byItem.keySet());

        return allRatedItems;
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        return getItemRatingsRated(idItem).keySet();
    }

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        if (newRatings_byUser.containsKey(idUser)) {
            Map<Long, RatingType> modifiedRatings = new TreeMap<>();

            modifiedRatings.putAll(originalRatingsDataset.getUserRatingsRated(idUser));
            modifiedRatings.putAll(newRatings_byUser.get(idUser));

            return modifiedRatings;

        } else {
            return originalRatingsDataset.getUserRatingsRated(idUser);
        }
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {

        if (newRatings_byItem.containsKey(idItem)) {
            Map<Long, RatingType> modifiedRatings = new TreeMap<>();

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

    public static <RatingType extends Rating> Map<Long, Map<Long, RatingType>> copyRatingsMap(Map<Long, Map<Long, RatingType>> newRatings) {

        Map<Long, Map<Long, RatingType>> newRatings_byUser = new TreeMap<>();
        for (long idUser : newRatings.keySet()) {
            for (Map.Entry<Long, RatingType> entry : newRatings.get(idUser).entrySet()) {
                long idItem = entry.getKey();
                RatingType rating = entry.getValue();

                if (idUser != rating.getIdUser()) {
                    throw new IllegalArgumentException("Rating '" + rating + "' does not match its indexation: idUser '" + idUser + "'");
                }
                if (idItem != rating.getIdItem()) {
                    throw new IllegalArgumentException("Rating '" + rating + "' does not match its indexation: idItem '" + idItem + "'");
                }

                if (!newRatings_byUser.containsKey(rating.getIdUser())) {
                    newRatings_byUser.put(rating.getIdUser(), new TreeMap<>());
                }

                if (newRatings_byUser.get(rating.getIdUser()).containsKey(rating.getIdItem())) {
                    RatingType duplicatedRating = newRatings_byUser.get(rating.getIdUser()).get(rating.getIdItem());
                    throw new IllegalArgumentException("Duplicated rating specified in the newRatings collection, '" + duplicatedRating + "' and '" + rating + "'");
                }
            }
        }
        return newRatings_byUser;
    }

    private Map<Long, Map<Long, RatingType>> changeIndexation(Map<Long, Map<Long, RatingType>> ratings) {
        Map<Long, Map<Long, RatingType>> changedIndexation = new TreeMap<>();

        for (long rowsIndex : ratings.keySet()) {
            for (long columnsIndex : ratings.get(rowsIndex).keySet()) {
                if (!changedIndexation.containsKey(columnsIndex)) {
                    changedIndexation.put(columnsIndex, new TreeMap<>());
                }

                changedIndexation.get(columnsIndex).put(rowsIndex, ratings.get(rowsIndex).get(columnsIndex));
            }
        }

        return changedIndexation;
    }
}
