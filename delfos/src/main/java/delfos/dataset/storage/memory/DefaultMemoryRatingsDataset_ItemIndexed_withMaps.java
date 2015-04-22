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
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 13-Mayo-2013
 * @version 1.2 21-Mayo-2013 Implementado mediante arboles.
 * @param <RatingType>
 */
public class DefaultMemoryRatingsDataset_ItemIndexed_withMaps<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final TreeSet<Integer> users;
    private final TreeMap<Integer, TreeMap<Integer, RatingType>> ratings_byItem;
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
            addRating(r.idUser, r.idItem, r);
        }
    }

    @Override
    public RatingType getRating(int idUser, int idItem) {
        RatingType ret = null;
        if (ratings_byItem.containsKey(idItem) && ratings_byItem.get(idItem).containsKey(idUser)) {
            ret = ratings_byItem.get(idItem).get(idUser);
        }

        return ret;
    }

    private void addRating(int idUser, int idItem, RatingType rating) {

        if (rc == null) {
            rc = new DecimalDomain(rating.ratingValue.doubleValue(), rating.ratingValue.doubleValue());
        }

        if (!ratings_byItem.containsKey(idItem)) {
            ratings_byItem.put(idItem, new TreeMap<>());
        }
        if (!users.contains(idUser)) {
            users.add(idUser);
        }

        ratings_byItem.get(idItem).put(idUser, rating);

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
        return ratings_byItem.keySet();
    }
    private boolean getUserRatingsWarningMessageShown = false;

    @Override
    public Set<Integer> getUserRated(Integer idUser) {
        if (!getUserRatingsWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getItemRated(Integer idItem):Collection<Integer>]");
            Global.showWarning(ratingDatasetEfficiencyException);
            getUserRatingsWarningMessageShown = true;
        }
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) {
        if (ratings_byItem.containsKey(idItem)) {
            return Collections.unmodifiableSet(ratings_byItem.get(idItem).keySet());
        } else {
            return Collections.EMPTY_SET;
        }
    }
    private boolean getUserRatingsRatedWarningMessageShown = false;

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) {
        if (!getUserRatingsRatedWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getItemRatingsRated(Integer idItem):Map<Integer, Byte>]");
            Global.showWarning(ratingDatasetEfficiencyException);
            getUserRatingsRatedWarningMessageShown = true;
        }
        Map<Integer, RatingType> ret = new TreeMap<>();
        ratings_byItem.keySet().stream().filter((idItem) -> (ratings_byItem.get(idItem).containsKey(idUser))).forEach((idItem) -> {
            ret.put(idItem, ratings_byItem.get(idItem).get(idUser));
        });
        return ret;
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) {
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
