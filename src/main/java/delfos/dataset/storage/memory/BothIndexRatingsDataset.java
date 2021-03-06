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
package delfos.dataset.storage.memory;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.user.User;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Dataset que almacena las valoraciones doblemente indexadas, es decir, por usuarios y por productos. De esta manera se
 * gana en eficiencia temporal a costa de utilizar una mayor cantidad de memoria ram
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.0 Unknow date
 * @param <RatingType>
 */
public class BothIndexRatingsDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    protected Map<Long, Map<Long, RatingType>> userIndex = new TreeMap<>();
    protected Map<Long, Map<Long, RatingType>> itemIndex = new TreeMap<>();
    protected int numRatings = 0;

    /**
     * Crea un dataset doblemente indexado (por usuarios y por productos) para ganar en eficiencia en tiempo utilizando
     * una mayor cantidad de memoria. El dataset está vacío iniciamlente
     */
    public BothIndexRatingsDataset() {
        //Se crea vacío
    }

    /**
     * Genera el dataset con las valoraciones de otro. Constructor por copia.
     *
     * @param ratingsDataset
     */
    public BothIndexRatingsDataset(RatingsDataset<? extends RatingType> ratingsDataset) {
        for (Long idUser : ratingsDataset.allUsers()) {
            try {
                Map<Long, ? extends RatingType> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);
                for (Long idItem : userRatingsRated.keySet()) {
                    RatingType rating = userRatingsRated.get(idItem);
                    addOneRating(rating);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
    }

    /**
     * Crea un dataset doblemente indexado (por usuarios y por productos) para ganar en eficiencia en tiempo utilizando
     * una mayor cantidad de memoria.
     *
     * @param ratingsIndexedByUser Valoraciones de todos los usuarios.
     */
    public BothIndexRatingsDataset(Map<Long, Map<Long, RatingType>> ratingsIndexedByUser) {
        for (Long idUser : ratingsIndexedByUser.keySet()) {
            for (Long idItem : ratingsIndexedByUser.get(idUser).keySet()) {
                RatingType rating = ratingsIndexedByUser.get(idUser).get(idItem);
                addOneRating(rating);
            }
        }
    }

    /**
     * Genera el dataset añadiendo valoraciones a un dataset.
     *
     * @param ratingsDataset dataset de origen
     * @param ratingsIndexedByUser valoraciones que se añaden.
     */
    public BothIndexRatingsDataset(RatingsDataset<RatingType> ratingsDataset, Map<Long, Map<Long, RatingType>> ratingsIndexedByUser) {

        checkDatasetsAreDisjointInUsers(ratingsDataset, ratingsIndexedByUser);

        try {
            for (Long idUser : ratingsDataset.allUsers()) {
                try {
                    Map<Long, RatingType> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);
                    for (Long idItem : userRatingsRated.keySet()) {
                        Rating rating = userRatingsRated.get(idItem);
                        addOneRating((RatingType) rating);
                    }
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }

            for (Long idUser : ratingsIndexedByUser.keySet()) {
                for (Long idItem : ratingsIndexedByUser.get(idUser).keySet()) {

                    RatingType rating = ratingsIndexedByUser.get(idUser).get(idItem);
                    addOneRating((RatingType) rating.clone());

                }
            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BothIndexRatingsDataset.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void checkDatasetsAreDisjointInUsers(RatingsDataset<RatingType> ratingsDataset, Map<Long, Map<Long, RatingType>> ratingsIndexedByUser) throws IllegalArgumentException {
        Set<Long> intersection = new TreeSet<>(ratingsDataset.allUsers());
        intersection.retainAll(ratingsIndexedByUser.keySet());
        if (!intersection.isEmpty()) {
            throw new IllegalArgumentException("The datasets share users: " + intersection);
        }
    }

    public BothIndexRatingsDataset(RatingsDataset<RatingType> ratingsDataset, Iterable<RatingType> ratings) {
        for (Long idUser : ratingsDataset.allUsers()) {
            try {
                Map<Long, RatingType> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);
                userRatingsRated.keySet().stream().map((idItem) -> userRatingsRated.get(idItem)).forEach((rating) -> {
                    addOneRating(rating);
                });
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        for (RatingType r : ratings) {
            addOneRating(r);
        }
    }

    public BothIndexRatingsDataset(Collection<RatingType> ratings) {
        checkUniqueIdUsers(ratings);
        checkUniqueIdItems(ratings);
        checkUniqueRatings(ratings);
        
        
        Map<User, List<RatingType>> usersWithRatings = ratings.parallelStream()
                .collect(Collectors.groupingBy(rating -> rating.getUser(), Collectors.toList()));

        Map<Long, Map<Long, RatingType>> ratingsByUser = usersWithRatings
                .entrySet().parallelStream()
                .filter(userRatingsEntry -> !userRatingsEntry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        userRatingsEntry -> userRatingsEntry.getKey().getId(),
                        userRatingsEntry -> userRatingsEntry.getValue().parallelStream().collect(Collectors.toMap(
                                rating -> rating.getItem().getId(),
                                rating -> rating)
                        )
                ));

        userIndex = ratingsByUser;

        Map<Item,List<RatingType>> itemsWithRatings = ratings.parallelStream()
                .collect(Collectors.groupingBy(rating -> rating.getItem(), Collectors.toList()));
        
        Map<Long, Map<Long, RatingType>> ratingsByItem = itemsWithRatings                
                .entrySet().parallelStream()
                .filter(itemRatingsEntry -> !itemRatingsEntry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        itemRatingsEntry -> itemRatingsEntry.getKey().getId(),
                        itemRatingsEntry -> itemRatingsEntry.getValue().parallelStream().collect(Collectors.toMap(
                                rating -> rating.getUser().getId(),
                                rating -> rating)
                        )
                ));

        itemIndex = ratingsByItem;

        numRatings = ratings.size();
    }

    private void checkUniqueIdUsers(Collection<RatingType> ratings) {
        Map<Long, List<User>> usersById= ratings.stream().map(rating -> rating.getUser()).collect(Collectors.groupingBy(
                user -> user.getId()));

        for (Map.Entry<Long, List<User>> entry : usersById.entrySet()) {
            Set<User> distinctUsers = entry.getValue().stream().collect(Collectors.toSet());
            if(distinctUsers.size()>1){
                throw new IllegalArgumentException("Multiple users with the same id, (noticed at user with id="+entry.getKey());
            }
        }
    }

    private void checkUniqueIdItems(Collection<RatingType> ratings) {
        Map<Long, List<Item>> itemsById= ratings.stream().map(rating -> rating.getItem()).collect(Collectors.groupingBy(
                item -> item.getId()));

        for (Map.Entry<Long, List<Item>> entry : itemsById.entrySet()) {
            Set<Item> distinctItems = entry.getValue().stream().collect(Collectors.toSet());
            if(distinctItems.size()>1){
                throw new IllegalArgumentException("Multiple items with the same id, (noticed at item with id="+entry.getKey());
            }
        }
    }

    private void checkUniqueRatings(Collection<RatingType> ratings) {
        Map<User, List<RatingType>> ratingsByUser = ratings.parallelStream().collect(
                Collectors.groupingBy(
                        rating -> rating.getUser(),
                        Collectors.toList()));

        List<User> usersWithDuplicateRatings = ratingsByUser
                .entrySet().parallelStream()
                .filter(entry -> {

                    List<RatingType> ratingsThisUser = entry.getValue();

                    long distinctRatingsThisUser = ratingsThisUser.parallelStream()
                            .map(rating -> rating.getIdItem())
                            .distinct().count();

                    return ratingsThisUser.size() != distinctRatingsThisUser;
                })
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());

        if (!usersWithDuplicateRatings.isEmpty()) {
            throw new IllegalStateException("There are duplicate ratings");
        }
    }

    @Override
    public RatingType getRating(long idUser, long idItem) {

        if (userIndex.containsKey(idUser) && userIndex.get(idUser).containsKey(idItem)) {
            RatingType rating = userIndex.get(idUser).get(idItem);
            return rating;
        } else {
            return null;
        }
    }

    protected final void addOneRating(RatingType rating) {
        final long idUser = rating.getIdUser();
        final long idItem = rating.getIdItem();

        //Añado el producto a la lista de productos.
        if (!itemIndex.containsKey(idItem)) {
            itemIndex.put(idItem, new TreeMap<>());
        }

        //Añado el usuario a la lista de usuarios.
        if (!userIndex.containsKey(idUser)) {
            userIndex.put(idUser, new TreeMap<>());
        }

        if (userIndex.get(idUser).containsKey(idItem)) {
            throw new IllegalArgumentException("The rating was already in the dataset");
        } else {
            userIndex.get(idUser).put(idItem, rating);
        }

        if (itemIndex.get(idItem).containsKey(idUser)) {
            throw new IllegalArgumentException("The rating was already in the dataset");
        } else {
            itemIndex.get(idItem).put(idUser, rating);
        }

        if (!(userIndex.get(idUser).get(idItem) == itemIndex.get(idItem).get(idUser))) {
            throw new IllegalArgumentException("User index and item index is different!");
        }
        numRatings++;
    }

    @Override
    public Set<Long> allUsers() {
        return new TreeSet<>(userIndex.keySet());
    }

    @Override
    public Set<Long> allRatedItems() {
        return new TreeSet<>(itemIndex.keySet());
    }

    @Override
    public Set<Long> getUserRated(long idUser) {
        if (userIndex.containsKey(idUser)) {
            return Collections.unmodifiableSet(getUserRatingsRated(idUser).keySet());
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public Set<Long> getItemRated(long idItem) {
        if (itemIndex.containsKey(idItem)) {
            return Collections.unmodifiableSet(getItemRatingsRated(idItem).keySet());
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) {
        if (userIndex.containsKey(idUser)) {
            Map<Long, RatingType> ret = userIndex.get(idUser);
            return Collections.unmodifiableMap(ret);
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) {
        if (itemIndex.containsKey(idItem)) {
            Map<Long, RatingType> ret = itemIndex.get(idItem);
            return Collections.unmodifiableMap(ret);
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    private Domain ratingsDomain = null;

    @Override
    public synchronized Domain getRatingsDomain() {
        if (ratingsDomain == null) {

            List<Double> distinctRatings = userIndex.values().parallelStream().flatMap(userRatings -> userRatings.values().stream())
                    .mapToDouble(rating -> rating.getRatingValue().doubleValue())
                    .distinct()
                    .boxed()
                    .sorted()
                    .collect(Collectors.toList());

            double maxRating = distinctRatings.stream().mapToDouble(v -> v).max().orElseThrow(() -> new IllegalStateException("No ratings in this dataset"));
            double minRating = distinctRatings.stream().mapToDouble(v -> v).min().orElseThrow(() -> new IllegalStateException("No ratings in this dataset"));

            ratingsDomain = new DecimalDomain(minRating, maxRating);
        }
        return ratingsDomain;
    }

    @Override
    public boolean isRatedItem(long idItem) {
        if (itemIndex.containsKey(idItem)) {
            return itemIndex.get(idItem).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public boolean isRatedUser(long idUser) {
        if (userIndex.containsKey(idUser)) {
            return userIndex.get(idUser).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public long getNumRatings() {
        return numRatings;
    }

}
