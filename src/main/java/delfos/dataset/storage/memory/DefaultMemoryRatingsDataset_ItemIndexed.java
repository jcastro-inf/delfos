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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
 * @param <RatingType>
 */
public class DefaultMemoryRatingsDataset_ItemIndexed<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final List<List<RatingType>> ratings;
    private final TreeSet<Long> users;
    private final TreeMap<Long, Integer> itemsIndex;
    private DecimalDomain rc = null;

    /**
     * Crea el dataset vacÃ­o.
     */
    public DefaultMemoryRatingsDataset_ItemIndexed() {
        super();
        ratings = new ArrayList<>();
        users = new TreeSet<>();
        itemsIndex = new TreeMap<>();
        rc = new DecimalDomain(1, 5);
    }

    public DefaultMemoryRatingsDataset_ItemIndexed(Iterable<RatingType> ratings) {
        this();
        for (RatingType r : ratings) {
            addRating(r.getIdUser(), r.getIdItem(), r);
        }
    }

    @Override
    public RatingType getRating(long idUser, long idItem) {
        RatingType rating = null;
        Iterator<RatingType> it = ratings.get(itemsIndex.get(idItem)).listIterator();
        while (it.hasNext() && rating == null) {
            RatingType _rating = it.next();
            if (_rating.getIdUser() == idUser && _rating.getIdItem() == idItem) {
                rating = _rating;
            }
        }

        return rating;
    }

    private void addRating(long idUser, long idItem, RatingType rating) {

        if (rc == null) {
            rc = new DecimalDomain(rating.getRatingValue().doubleValue(), rating.getRatingValue().doubleValue());
        }

        if (!itemsIndex.containsKey(idItem)) {
            ratings.add(new ArrayList<>());
            itemsIndex.put(idItem, ratings.size() - 1);
        }
        if (!users.contains(idUser)) {
            users.add(idUser);
        }
        ratings.get(itemsIndex.get(idItem)).add(rating);

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
        return itemsIndex.keySet();
    }
    private boolean getUserRatingsWarningMessageShown = false;

    @Override
    public Set<Long> getUserRated(long idUser) {
        if (!getUserRatingsWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getUserRated(Long idUser):Collection<Long>]");
            Global.showWarning(ratingDatasetEfficiencyException);

            getUserRatingsWarningMessageShown = true;
        }
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Set<Long> getItemRated(long idItem) {
        return getItemRatingsRated(idItem).keySet();
    }
    private boolean getUserRatingsRatedWarningMessageShown = false;

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) {
        if (!getUserRatingsRatedWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getUserRatingsRated(Long idUser):Map<Long, Byte>]");
            Global.showWarning(ratingDatasetEfficiencyException);

            getUserRatingsRatedWarningMessageShown = true;
        }
        Map<Long, RatingType> ret = new TreeMap<>();
        for (int i = 0; i < itemsIndex.size(); i++) {
            for (Iterator<RatingType> it = ratings.get(i).listIterator(); it.hasNext();) {
                RatingType rating = it.next();

                if (rating.getIdUser() == idUser) {
                    ret.put(rating.getIdItem(), rating);
                }
            }
        }
        return ret;
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) {

        Map<Long, RatingType> ret = new TreeMap<>();
        Integer index = itemsIndex.get(idItem);
        if (index == null) {
            return new TreeMap<>();
        }
        List<RatingType> itemRow = ratings.get(index);

        for (Iterator<RatingType> it = itemRow.listIterator(); it.hasNext();) {
            RatingType rating = it.next();
            if (rating.getIdItem() == idItem) {
                ret.put(rating.getIdUser(), rating);
            }
        }

        return ret;
    }

    @Override
    public Domain getRatingsDomain() {
        return rc;
    }

    public void setRatingsDomain(DecimalDomain rc) {
        this.rc = rc;
    }
}
