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

import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.user.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Dataset para la validación de
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknow date
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.2 06-Mar-2013 Modificación de los parámetros del constructor y corrección de errores.
 * @param <RatingType>
 */
public class TrainingRatingsDataset_CPU<RatingType extends Rating>
        extends RatingsDatasetAdapter<RatingType>
        implements TrainingRatingsDataset<RatingType> {

    private final Map<User, Set<Item>> testRatings_byUser;
    private final RatingsDataset<RatingType> originalDataset;
    private Set<Long> allRatedItems;
    /**
     * Crea un buffer para no tener que recalcular los conjuntos indizados por item. Acelera la ejecución del metodo
     * item item
     */
    private final Map<Long, Map<Long, RatingType>> bufferItems = Collections.synchronizedMap(new TreeMap<Long, Map<Long, RatingType>>());

    public TrainingRatingsDataset_CPU(RatingsDataset<RatingType> originalDataset, Map<User, Set<Item>> testSet) throws UserNotFound, ItemNotFound {
        super();
        this.originalDataset = originalDataset;
        this.testRatings_byUser = testSet;
        for (User user : testSet.keySet()) {
            for (Item item : testSet.get(user)) {
                if (originalDataset.getRating(user.getId(), item.getId()) == null) {
                    Map<Long, RatingType> userRated = originalDataset.getUserRatingsRated(user.getId());
                    if (userRated.isEmpty()) {
                        Global.showWarning("User " + user + "hasn't rated any items.");
                    }
                    Map<Long, RatingType> itemRated = originalDataset.getItemRatingsRated(item.getId());
                    if (itemRated.isEmpty()) {
                        Global.showWarning("Item " + item + "hasn't received any rating.");
                    }
                    throw new IllegalArgumentException("Specified rating (idUser=" + user.getId() + ",idItem=" + item.getId() + ") not found in originalDataset");
                }
            }
        }
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        if (testRatings_byUser.containsKey(idUser) && testRatings_byUser.get(idUser).contains(idItem)) {
            return null;
        } else {
            return originalDataset.getRating(idUser, idItem);
        }
    }

    @Override
    public Set<Long> allUsers() {
        return originalDataset.allUsers();
    }

    @Override
    public synchronized Set<Long> allRatedItems() {
        if (this.allRatedItems == null) {
            allRatedItems = this.allUsers().parallelStream()
                    .flatMap(user -> this.getUserRated(user).parallelStream())
                    .collect(Collectors.toSet());

            allRatedItems = Collections.unmodifiableSet(allRatedItems);
        }

        return Collections.unmodifiableSet(allRatedItems);
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        Set<Long> ret = new TreeSet<>(originalDataset.getUserRated(idUser));
        if (testRatings_byUser.containsKey(idUser)) {
            ret.removeAll(testRatings_byUser.get(idUser));
        }
        return ret;
    }

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {

        TreeMap<Long, RatingType> ret = new TreeMap<>(originalDataset.getUserRatingsRated(idUser));

        if (testRatings_byUser.containsKey(idUser)) {
            final Set<Item> ratingsInTestSet = testRatings_byUser.get(idUser);
            for (Item item : ratingsInTestSet) {
                ret.remove(item.getId());
            }
        }

        return ret;
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        return getItemRatingsRated(idItem).keySet();
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {
        if (bufferItems.containsKey(idItem)) {
            return bufferItems.get(idItem);
        } else {

            Map<Long, RatingType> ret = new TreeMap<>();
            Map<Long, RatingType> itemRatingsRated = originalDataset.getItemRatingsRated(idItem);
            for (Long idUser : itemRatingsRated.keySet()) {

                if (testRatings_byUser.containsKey(idUser) && testRatings_byUser.get(idUser).contains(idItem)) {
                    //está en el test
                } else {
                    //está en el train
                    ret.put(idUser, itemRatingsRated.get(idUser));
                }
            }

            bufferItems.put(idItem, ret);
            return ret;
        }
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
