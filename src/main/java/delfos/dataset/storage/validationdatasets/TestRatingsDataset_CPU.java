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
package delfos.dataset.storage.validationdatasets;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.user.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dataset que deja visibles las valoraciones especificadas en el conjunto de datos indicado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknow date
 * @version 1.2 06-Mar-2013 Modificación de los parámetros del constructor y corrección de errores.
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @param <RatingType>
 */
public class TestRatingsDataset_CPU<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> implements TestRatingsDataset<RatingType> {

    /**
     * Valoraciones de test. Son las valoraciones que son accesibles.
     */
    private final Map<User, Set<Item>> testRatings_byUser;

    private final Map<Long, User > userById;
    private final Map<Long, Item> itemById;
    /**
     * Dataset original que contiene el conjunto de datos completo.
     */
    private final RatingsDataset<RatingType> originalDataset;
    /**
     * Buffer para almacenar el conjunto de productos valorados.
     */
    private Set<Item> allRatedItems;

    public TestRatingsDataset_CPU(RatingsDataset<RatingType> originalDatset, Map<User, Set<Item>> testSet) throws UserNotFound, ItemNotFound {
        super();
        this.originalDataset = originalDatset;

        this.testRatings_byUser = testSet;
        userById = new HashMap<>() ;
        itemById = new HashMap<>();

        for(RatingType rating : originalDataset){
            userById.put(rating.getUser().getId(), rating.getUser());
            itemById.put(rating.getItem().getId(),rating.getItem());
        }
        for (User user : testSet.keySet()) {
            for (Item item : testSet.get(user)) {
                if (originalDatset.getRating(user.getId(),item.getId()) == null) {
                    throw new IllegalArgumentException("Specified rating isn't found in originalDataset");
                }
            }
        }
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        User user = userById.get(idUser);
        Item item = itemById.get(idItem);
        if (testRatings_byUser.containsKey(user) && testRatings_byUser.get(user).contains(item)) {
            return originalDataset.getRating(idUser, idItem);
        } else {
            return null;
        }
    }

    @Override
    public Set<Long> allUsers() {
        return testRatings_byUser.keySet().stream().map(user-> user.getId()).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> allRatedItems() {
        if (this.allRatedItems == null) {
            allRatedItems = new TreeSet<>();

            for (User user: testRatings_byUser.keySet()) {
                allRatedItems.addAll(testRatings_byUser.get(user));
            }
        }

        return Collections.unmodifiableSet(allRatedItems.stream().map(item-> item.getId()).collect(Collectors.toSet()));
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        User user  = userById.get(idUser);
        if (testRatings_byUser.containsKey(user)) {
            Set<Long> ret = new TreeSet<>();
            ret.addAll(testRatings_byUser.get(user).stream().map(item-> item.getId()).collect(Collectors.toSet()));
            return ret;
        } else {
            throw new UserNotFound(idUser);
        }
    }

    private final Set<Long> usersWarned = Collections.synchronizedSet(new TreeSet<>());

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        User user = userById.get(idUser);
        if (!testRatings_byUser.containsKey(user)) {
            synchronized (usersWarned) {
                if (!usersWarned.contains(idUser)) {
                    usersWarned.add(idUser);
                    if (originalDataset.allUsers().contains(idUser)) {
                        Global.showWarning("User " + idUser + " has no ratings in the test set.");
                    } else {
                        throw new UserNotFound(idUser);
                    }
                }
            }
            return Collections.EMPTY_MAP;
        }

        Map<Long, RatingType> userRatingsRated = testRatings_byUser.get(user).parallelStream()
                .collect(Collectors.toMap(
                        item -> item.getId(),
                        item -> {
                            try {
                                return originalDataset.getRating(idUser, item.getId());
                            } catch (ItemNotFound ex) {
                                Global.showError(ex);
                                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                                throw new IllegalArgumentException(ex);
                            }
                        }
                ));

        return userRatingsRated;
    }

    @Override
    public Set<Long> getItemRated(long idItem) {
        Set<Long> ret = new TreeSet<>();

        testRatings_byUser
                .keySet().stream()
                .filter(
                        (user) -> {
                            Item item = itemById.get(idItem);
                            return testRatings_byUser.get(user).contains(item);
                        })
                .forEach((user) -> {
                    ret.add(user.getId());
                });
        return ret;
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {
        Map<Long, RatingType> ret = new TreeMap<>();

        Item item  = itemById.get(idItem);
        for (User user  : testRatings_byUser.keySet()) {
            if (testRatings_byUser.get(user).contains(item)) {
                try {
                    ret.put(user.getId(), originalDataset.getRating(user.getId(), idItem));
                } catch (UserNotFound ex) {
                    Global.showError(ex);
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }
        }
        return ret;
    }

    @Override
    public Domain getRatingsDomain() {
        return originalDataset.getRatingsDomain();
    }

    @Override
    public RatingsDataset<RatingType> getOriginalDataset() {
        return originalDataset;
    }
}
