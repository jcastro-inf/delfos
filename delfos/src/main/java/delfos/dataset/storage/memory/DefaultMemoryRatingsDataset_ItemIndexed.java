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
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 13-Mayo-2013
 * @param <RatingType>
 */
public class DefaultMemoryRatingsDataset_ItemIndexed<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final List<List<RatingType>> ratings;
    private final TreeSet<Integer> users;
    private final TreeMap<Integer, Integer> itemsIndex;
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
            addRating(r.idUser, r.idItem, r);
        }
    }

    @Override
    public RatingType getRating(int idUser, int idItem) {
        RatingType rating = null;
        Iterator<RatingType> it = ratings.get(itemsIndex.get(idItem)).listIterator();
        while (it.hasNext() && rating == null) {
            RatingType _rating = it.next();
            if (_rating.idUser == idUser && _rating.idItem == idItem) {
                rating = _rating;
            }
        }

        return rating;
    }

    private void addRating(int idUser, int idItem, RatingType rating) {

        if (rc == null) {
            rc = new DecimalDomain(rating.ratingValue.doubleValue(), rating.ratingValue.doubleValue());
        }

        if (!itemsIndex.containsKey(idItem)) {
            ratings.add(new ArrayList<>());
            itemsIndex.put(idItem, ratings.size() - 1);
        }
        if (!users.contains(idUser)) {
            users.add(idUser);
        }
        ratings.get(itemsIndex.get(idItem)).add(rating);

        if (rating.ratingValue.floatValue() < rc.min()) {
            rc = new DecimalDomain(rating.ratingValue.floatValue(), rc.max());
        }
        if (rating.ratingValue.floatValue() > rc.max()) {
            rc = new DecimalDomain(rc.min(), rating.ratingValue.floatValue());
        }
    }

    @Override
    public Set<Integer> allUsers() {
        return Collections.unmodifiableSet(users);
    }

    @Override
    public Set<Integer> allRatedItems() {
        return itemsIndex.keySet();
    }
    private boolean getUserRatingsWarningMessageShown = false;

    @Override
    public Collection<Integer> getUserRated(Integer idUser) {
        if (!getUserRatingsWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getUserRated(Integer idUser):Collection<Integer>]");
            Global.showWarning(ratingDatasetEfficiencyException);

            getUserRatingsWarningMessageShown = true;
        }
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) {
        return getItemRatingsRated(idItem).keySet();
    }
    private boolean getUserRatingsRatedWarningMessageShown = false;

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) {
        if (!getUserRatingsRatedWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getUserRatingsRated(Integer idUser):Map<Integer, Byte>]");
            Global.showWarning(ratingDatasetEfficiencyException);

            getUserRatingsRatedWarningMessageShown = true;
        }
        Map<Integer, RatingType> ret = new TreeMap<>();
        for (int i = 0; i < itemsIndex.size(); i++) {
            for (Iterator<RatingType> it = ratings.get(i).listIterator(); it.hasNext();) {
                RatingType rating = it.next();

                if (rating.idUser == idUser) {
                    ret.put(rating.idItem, rating);
                }
            }
        }
        return ret;
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) {

        Map<Integer, RatingType> ret = new TreeMap<>();
        Integer index = itemsIndex.get(idItem);
        if (index == null) {
            return new TreeMap<>();
        }
        List<RatingType> itemRow = ratings.get(index);

        for (Iterator<RatingType> it = itemRow.listIterator(); it.hasNext();) {
            RatingType rating = it.next();
            if (rating.idItem == idItem) {
                ret.put(rating.idUser, rating);
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
