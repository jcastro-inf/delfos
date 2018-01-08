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

import delfos.common.Global;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingDatasetEfficiencyException;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Dataset que almacena los datos indexados por productos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 13-Mayo-2013
 * @version 1.2 21-Mayo-2013 Implementado mediante arboles.
 * @param <RatingType>
 */
public class DefaultMemoryRatingsDataset_ItemIndexed_withMaps<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final TreeSet<Long> users;
    private final TreeMap<Long, TreeMap<Long, RatingType>> ratings_byItem;
    private DecimalDomain rc = null;

    /**
     * Crea el dataset vacío.
     */
    public DefaultMemoryRatingsDataset_ItemIndexed_withMaps() {
        super();
        users = new TreeSet<>();
        ratings_byItem = new TreeMap<>();
        rc = new DecimalDomain(1, 5);
    }

    public DefaultMemoryRatingsDataset_ItemIndexed_withMaps(Iterable<RatingType> ratings) {
        this();
        for (RatingType r : ratings) {
            addRating(r.getIdUser(), r.getIdItem(), r);
        }
    }

    @Override
    public RatingType getRating(long idUser, long idItem) {
        RatingType ret = null;
        if (ratings_byItem.containsKey(idItem) && ratings_byItem.get(idItem).containsKey(idUser)) {
            ret = ratings_byItem.get(idItem).get(idUser);
        }

        return ret;
    }

    private void addRating(long idUser, long idItem, RatingType rating) {

        if (rc == null) {
            rc = new DecimalDomain(rating.getRatingValue().doubleValue(), rating.getRatingValue().doubleValue());
        }

        if (!ratings_byItem.containsKey(idItem)) {
            ratings_byItem.put(idItem, new TreeMap<>());
        }
        if (!users.contains(idUser)) {
            users.add(idUser);
        }

        ratings_byItem.get(idItem).put(idUser, rating);

        if (rating.getRatingValue().doubleValue() < rc.min()) {
            rc = new DecimalDomain(rating.getRatingValue().doubleValue(), rc.max());
        }
        if (rating.getRatingValue().doubleValue() > rc.max()) {
            rc = new DecimalDomain(rc.min(), rating.getRatingValue().doubleValue());
        }
    }

    @Override
    public Set<Long> allUsers() {
        return Collections.unmodifiableSet(users);
    }

    @Override
    public Set<Long> allRatedItems() {
        return ratings_byItem.keySet();
    }
    private boolean getUserRatingsWarningMessageShown = false;

    @Override
    public Set<Long> getUserRated(long idUser) {
        if (!getUserRatingsWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getItemRated(Integer idItem):Collection<Integer>]");
            Global.showWarning(ratingDatasetEfficiencyException);
            getUserRatingsWarningMessageShown = true;
        }
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Set<Long> getItemRated(long idItem) {
        if (ratings_byItem.containsKey(idItem)) {
            return Collections.unmodifiableSet(ratings_byItem.get(idItem).keySet());
        } else {
            return Collections.EMPTY_SET;
        }
    }
    private boolean getUserRatingsRatedWarningMessageShown = false;

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) {
        if (!getUserRatingsRatedWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getItemRatingsRated(Integer idItem):Map<Integer, Byte>]");
            Global.showWarning(ratingDatasetEfficiencyException);
            getUserRatingsRatedWarningMessageShown = true;
        }
        Map<Long, RatingType> ret = new TreeMap<>();
        ratings_byItem.keySet().stream().filter((idItem) -> (ratings_byItem.get(idItem).containsKey(idUser))).forEach((idItem) -> {
            ret.put(idItem, ratings_byItem.get(idItem).get(idUser));
        });
        return ret;
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) {
        if (ratings_byItem.containsKey(idItem)) {
            return ratings_byItem.get(idItem);
        } else {
            return new TreeMap<>();
        }
    }

    @Override
    public Domain getRatingsDomain() {
        return rc;
    }

    public void setRatingsDomain(DecimalDomain rc) {
        this.rc = rc;
    }
}
