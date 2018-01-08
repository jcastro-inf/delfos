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
package delfos.dataset.generated.modifieddatasets.pseudouser;

import delfos.common.StringsOrderings;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.user.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RatingType>
 */
public class PseudoUserRatingsDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final DatasetLoader<RatingType> originalDatasetLoader;
    private final Map<User, Map<Item, RatingType>> pseudoUsersRatings;

    private final Map<Long, User> pseudoUsersById;

    public PseudoUserRatingsDataset(DatasetLoader<RatingType> originalDatasetLoader, Map<User, Map<Item, RatingType>> pseudoUsersRatings) {

        this.originalDatasetLoader = originalDatasetLoader;
        this.pseudoUsersRatings = pseudoUsersRatings;

        pseudoUsersById = pseudoUsersRatings.keySet().stream().collect(Collectors.toMap(user -> user.getId(), user -> user));
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {

        RatingType rating = originalDatasetLoader.getRatingsDataset().getRating(idUser, idItem);
        if (rating != null) {
            return rating;
        }

        User user = pseudoUsersRatings.keySet().stream()
                .filter(user2 -> user2.getId() == idUser)
                .findAny()
                .orElse(null);
        if (user == null) {
            return null;
        }

        Item item = originalDatasetLoader.getContentDataset().get(idItem);

        if (pseudoUsersRatings.get(user).containsKey(item)) {
            return pseudoUsersRatings.get(user).get(item);
        } else {
            return null;
        }
    }

    @Override
    public Set<Long> allUsers() {
        Set<Long> allUsers = new TreeSet<>();

        allUsers.addAll(originalDatasetLoader.getRatingsDataset().allUsers());
        allUsers.addAll(pseudoUsersRatings.keySet().stream()
                .map(user -> user.getId()).collect(Collectors.toSet()));
        return allUsers;
    }

    @Override
    public Set<Long> allRatedItems() {

        Set<Long> allRatedItems = new TreeSet<>();

        allRatedItems.addAll(originalDatasetLoader.getRatingsDataset().allRatedItems());

        Set<Long> itemsRatedByPseudoUsers = pseudoUsersRatings.values().stream()
                .flatMap(userRatings -> userRatings.keySet().stream())
                .map(item -> item.getId())
                .collect(Collectors.toSet());

        allRatedItems.addAll(itemsRatedByPseudoUsers);

        return allRatedItems;

    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        return getItemRatingsRated(idUser).keySet();
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        return getItemRatingsRated(idItem).keySet();
    }

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        boolean containsKey = pseudoUsersById.containsKey(idUser);

        if (containsKey) {
            User pseudoUser = pseudoUsersById.get(idUser);
            Map<Long, RatingType> pseudoUserRatings
                    = pseudoUsersRatings.get(pseudoUser)
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().getId(),
                            entry -> entry.getValue()));

            return pseudoUserRatings;
        } else {
            return originalDatasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
        }
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {

        Map<Long, RatingType> itemRatingsRated = originalDatasetLoader.getRatingsDataset().getItemRatingsRated(idItem);

        Collection<RatingType> ratedByPseudoUsers = pseudoUsersRatings.values().parallelStream()
                .flatMap(pseudoUserRatings -> pseudoUserRatings.values().stream())
                .filter(rating -> rating.getIdItem() == idItem)
                .collect(Collectors.toList());

        Map<Long, RatingType> itemsRatingsRated_byPseudoUsers = ratedByPseudoUsers
                .stream().collect(Collectors.toMap(
                        rating -> rating.getIdItem(),
                        rating -> rating));

        Map<Long, RatingType> ret = new TreeMap<>();

        ret.putAll(itemRatingsRated);
        ret.putAll(itemsRatingsRated_byPseudoUsers);

        return ret;
    }

    @Override
    public Domain getRatingsDomain() {
        return originalDatasetLoader.getRatingsDataset().getRatingsDomain();
    }

    @Override
    public synchronized int hashCode() {

        int originalDatasetHash = originalDatasetLoader.getRatingsDataset().hashCode();

        HashCodeBuilder pseudoRatingsHashCodeBuilder = new HashCodeBuilder(37, 11);

        List<RatingType> ratingsSorted = pseudoUsersRatings.values().stream()
                .flatMap(userRatings -> userRatings.values().stream())
                .sorted((rating, rating2) -> StringsOrderings.compareNatural(rating.toString(), rating2.toString()))
                .collect(Collectors.toList());

        for (RatingType rating : ratingsSorted) {
            String ratingToString = rating.toString();
            pseudoRatingsHashCodeBuilder.append(ratingToString);
        }

        ratingsSorted.stream().forEachOrdered(rating -> pseudoRatingsHashCodeBuilder.append(rating.toString()));

        int pseudoRatingsHashCode = pseudoRatingsHashCodeBuilder.build();

        HashCodeBuilder finalHash = new HashCodeBuilder(37, 11);

        int finalHashValue = finalHash.append(originalDatasetHash).append(pseudoRatingsHashCode).build();

        return finalHashValue;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PseudoUserRatingsDataset<?> other = (PseudoUserRatingsDataset<?>) obj;
        if (!Objects.equals(this.originalDatasetLoader, other.originalDatasetLoader)) {
            return false;
        }
        if (!Objects.equals(this.pseudoUsersRatings, other.pseudoUsersRatings)) {
            return false;
        }
        return true;
    }

}
