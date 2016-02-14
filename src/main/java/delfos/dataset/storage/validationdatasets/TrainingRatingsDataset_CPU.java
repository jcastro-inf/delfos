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
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Dataset para la validación de
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknow date
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.2 06-Mar-2013 Modificación de los parámetros del constructor y
 * corrección de errores.
 * @param <RatingType>
 */
public class TrainingRatingsDataset_CPU<RatingType extends Rating>
        extends RatingsDatasetAdapter<RatingType>
        implements TrainingRatingsDataset<RatingType> {

    private final Map<Integer, Set<Integer>> testRatings_byUser;
    private final RatingsDataset<RatingType> originalDataset;
    private Set<Integer> allRatedItems;
    /**
     * Crea un buffer para no tener que recalcular los conjuntos indizados por
     * item. Acelera la ejecución del metodo item item
     */
    private final Map<Integer, Map<Integer, RatingType>> bufferItems = Collections.synchronizedMap(new TreeMap<Integer, Map<Integer, RatingType>>());

    public TrainingRatingsDataset_CPU(RatingsDataset<RatingType> originalDataset, Map<Integer, Set<Integer>> testSet) throws UserNotFound, ItemNotFound {
        super();
        this.originalDataset = originalDataset;
        this.testRatings_byUser = testSet;
        for (int idUser : testSet.keySet()) {
            for (int idItem : testSet.get(idUser)) {
                if (originalDataset.getRating(idUser, idItem) == null) {
                    Collection<Integer> userRated = originalDataset.getUserRated(idUser);
                    if (userRated.isEmpty()) {
                        Global.showWarning("User " + idUser + "hasn't rated any items.");
                    }
                    Collection<Integer> itemRated = originalDataset.getItemRated(idItem);
                    if (itemRated.isEmpty()) {
                        Global.showWarning("Item " + idItem + "hasn't received any rating.");
                    }
                    throw new IllegalArgumentException("Specified rating (idUser=" + idUser + ",idItem=" + idItem + ") not found in originalDataset");
                }
            }
        }
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        if (testRatings_byUser.containsKey(idUser) && testRatings_byUser.get(idUser).contains(idItem)) {
            return null;
        } else {
            return originalDataset.getRating(idUser, idItem);
        }
    }

    @Override
    public Set<Integer> allUsers() {
        return originalDataset.allUsers();
    }

    @Override
    public synchronized Set<Integer> allRatedItems() {
        if (this.allRatedItems == null) {
            allRatedItems = Collections.synchronizedSet(new TreeSet<Integer>());

            for (Rating rating : this) {
                allRatedItems.add(rating.getIdItem());
            }

            allRatedItems = Collections.unmodifiableSet(allRatedItems);
        }

        return Collections.unmodifiableSet(allRatedItems);
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) throws UserNotFound {
        Set<Integer> ret = new TreeSet<>(originalDataset.getUserRated(idUser));
        if (testRatings_byUser.containsKey(idUser)) {
            ret.removeAll(testRatings_byUser.get(idUser));
        }
        return ret;
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {

        TreeMap<Integer, RatingType> ret = new TreeMap<>(originalDataset.getUserRatingsRated(idUser));

        if (testRatings_byUser.containsKey(idUser)) {
            for (int idItem : testRatings_byUser.get(idUser)) {
                ret.remove(idItem);
            }
        }

        return ret;
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        return getItemRatingsRated(idItem).keySet();
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        if (bufferItems.containsKey(idItem)) {
            return bufferItems.get(idItem);
        } else {

            Map<Integer, RatingType> ret = new TreeMap<>();
            Map<Integer, RatingType> itemRatingsRated = originalDataset.getItemRatingsRated(idItem);
            for (Integer idUser : itemRatingsRated.keySet()) {

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
