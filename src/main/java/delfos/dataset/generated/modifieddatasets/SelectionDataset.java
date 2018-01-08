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
package delfos.dataset.generated.modifieddatasets;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps a dataset and make visible only the users and items specified as allowed.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @param <RatingType> Type of the rating of this dataset.
 */
public class SelectionDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private Collection<Long> allowedUsers = Collections.EMPTY_SET;
    private Collection<Long> allowedItems = Collections.EMPTY_SET;
    private RatingsDataset<RatingType> originalDataset;

    public SelectionDataset(RatingsDataset<RatingType> ratingsDataset) {
        super();
        this.originalDataset = ratingsDataset;
    }

    public void setOriginalDataset(RatingsDataset<RatingType> originalDataset) {
        this.originalDataset = originalDataset;
    }

    public void setAllowedItems(Collection<Long> allowedItems) {
        //Comprobar si el conjunto de productos existe en el dataset
        List<Long> notInOriginal = allowedItems.parallelStream()
                .filter(allowedItem -> !originalDataset.allRatedItems().contains(allowedItem))
                .sorted().collect(Collectors.toList());

        if (!notInOriginal.isEmpty()) {
            Global.showWarning(notInOriginal.size() + " items not in original dataset: " + notInOriginal.toString());
        }

        this.allowedItems = allowedItems.parallelStream().collect(Collectors.toSet());
    }

    public void setAllowedUsers(Collection<Long> allowedUsers) {
        //Comprobar si el conjunto de usuarios existe en el dataset
        List<Long> notInOriginal = allowedUsers.parallelStream()
                .filter(allowedItem -> !originalDataset.allUsers().contains(allowedItem))
                .sorted().collect(Collectors.toList());

        if (!notInOriginal.isEmpty()) {
            Global.showWarning(notInOriginal.size() + " items not in original dataset: " + notInOriginal.toString());
        }

        this.allowedUsers = allowedUsers.parallelStream().collect(Collectors.toSet());
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        if (!allowedUsers.contains(idUser)) {
            throw new UserNotFound(idUser);
        }
        if (!allowedItems.contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
        return originalDataset.getRating(idUser, idItem);
    }

    @Override
    public Set<Long> allUsers() {
        Set<Long> ratedUsers = allowedUsers
                .parallelStream()
                .filter((idUser) -> !getUserRated(idUser).isEmpty()).
                collect(Collectors.toSet());
        return ratedUsers;
    }

    @Override
    public Set<Long> allRatedItems() {
        Set<Long> ratedItems = allowedItems
                .parallelStream()
                .filter(idItem -> isRatedItem(idItem))
                .collect(Collectors.toSet());
        return ratedItems;
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        if (!allowedUsers.contains(idUser)) {
            throw new UserNotFound(idUser);
        }
        Set<Long> userRated = originalDataset.getUserRated(idUser).parallelStream()
                .filter(item -> allowedItems.contains(item))
                .collect(Collectors.toSet());

        return userRated;

    }

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        if (!allowedUsers.contains(idUser)) {
            throw new UserNotFound(idUser);
        }

        Map<Long, RatingType> userRatingsRated = originalDataset.getUserRatingsRated(idUser).values()
                .parallelStream()
                .filter(rating -> allowedItems.contains(rating.getItem().getId()))
                .collect(Collectors.toMap(
                        rating -> rating.getItem().getId(),
                        rating -> rating)
                );

        return userRatingsRated;
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        if (!allowedItems.contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
        Set<Long> itemRatingsRated = originalDataset.getItemRated(idItem).parallelStream()
                .filter(user -> allowedUsers.contains(user))
                .collect(Collectors.toSet());
        return itemRatingsRated;
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {
        if (!allowedItems.contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
        Map<Long, RatingType> itemRatingsRated = originalDataset.getItemRatingsRated(idItem).values().parallelStream()
                .filter(rating -> allowedUsers.contains(rating.getUser().getId()))
                .collect(Collectors.toMap(
                        rating -> rating.getUser().getId(),
                        rating -> rating)
                );

        return itemRatingsRated;
    }

    @Override
    public Domain getRatingsDomain() {
        return originalDataset.getRatingsDomain();
    }

    @Override
    public long getNumRatings() {
        long size = 0;
        for (long idUser : allowedUsers) {
            try {
                size += getUserRated(idUser).size();
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        return size;
    }

}
