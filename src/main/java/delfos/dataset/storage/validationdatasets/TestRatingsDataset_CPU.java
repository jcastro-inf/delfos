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
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
    private final Map<Long, Set<Long>> testRatings_byUser;
    /**
     * Dataset original que contiene el conjunto de datos completo.
     */
    private final RatingsDataset<RatingType> originalDataset;
    /**
     * Buffer para almacenar el conjunto de productos valorados.
     */
    private Set<Long> allRatedItems;

    public TestRatingsDataset_CPU(RatingsDataset<RatingType> originalDatset, Map<Long, Set<Long>> testSet) throws UserNotFound, ItemNotFound {
        super();
        this.originalDataset = originalDatset;

        this.testRatings_byUser = testSet;
        for (long idUser : testSet.keySet()) {
            for (long idItem : testSet.get(idUser)) {
                if (originalDatset.getRating(idUser, idItem) == null) {
                    throw new IllegalArgumentException("Specified rating isn't found in originalDataset");
                }
            }
        }
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        if (testRatings_byUser.containsKey(idUser) && testRatings_byUser.get(idUser).contains(idItem)) {
            return originalDataset.getRating(idUser, idItem);
        } else {
            return null;
        }
    }

    @Override
    public Set<Long> allUsers() {
        return testRatings_byUser.keySet();
    }

    @Override
    public Set<Long> allRatedItems() {
        if (this.allRatedItems == null) {
            allRatedItems = new TreeSet<>();

            for (Long idUser : testRatings_byUser.keySet()) {
                allRatedItems.addAll(testRatings_byUser.get(idUser));
            }
        }

        return Collections.unmodifiableSet(allRatedItems);
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        if (testRatings_byUser.containsKey(idUser)) {
            Set<Long> ret = new TreeSet<>();
            ret.addAll(testRatings_byUser.get(idUser));
            return ret;
        } else {
            throw new UserNotFound(idUser);
        }
    }

    private final Set<Long> usersWarned = Collections.synchronizedSet(new TreeSet<>());

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        if (!testRatings_byUser.containsKey(idUser)) {
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

        Map<Long, RatingType> userRatingsRated = testRatings_byUser.get(idUser).parallelStream()
                .collect(Collectors.toMap(
                        idItem -> idItem,
                        idItem -> {
                            try {
                                return originalDataset.getRating(idUser, idItem);
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
                        (idUser) -> (testRatings_byUser.get(idUser).contains(idItem)))
                .forEach((idUser) -> {
                    ret.add(idUser);
                });
        return ret;
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {
        Map<Long, RatingType> ret = new TreeMap<>();

        for (long idUser : testRatings_byUser.keySet()) {
            if (testRatings_byUser.get(idUser).contains(idItem)) {
                try {
                    ret.put(idUser, originalDataset.getRating(idUser, idItem));
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
