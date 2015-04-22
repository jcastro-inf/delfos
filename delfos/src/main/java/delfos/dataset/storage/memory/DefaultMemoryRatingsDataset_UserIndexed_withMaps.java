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
 * Datase que almacena los datos de manera desordenada. Solo calcula los
 * conjuntos de items valorados y usuarios que han valorado. El resto de los
 * métodos tienen un orden de eficiencia lineal.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknow date
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.2 21-Mayo-2013 Implementado mediante arboles.
 * @param <RatingType>
 */
public class DefaultMemoryRatingsDataset_UserIndexed_withMaps<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final TreeSet<Integer> items;
    private final TreeMap<Integer, TreeMap<Integer, RatingType>> ratings_byUser;
    private DecimalDomain rc = null;

    /**
     * Crea el dataset vacío.
     */
    public DefaultMemoryRatingsDataset_UserIndexed_withMaps() {
        super();
        items = new TreeSet<>();
        ratings_byUser = new TreeMap<>();
        rc = new DecimalDomain(1, 5);
    }

    public DefaultMemoryRatingsDataset_UserIndexed_withMaps(Iterable<RatingType> ratings) {
        this();
        for (RatingType r : ratings) {
            addRating(r.idUser, r.idItem, r);
        }
    }

    @Override
    public RatingType getRating(int idUser, int idItem) {
        RatingType ret = null;
        if (ratings_byUser.containsKey(idUser) && ratings_byUser.get(idUser).containsKey(idItem)) {
            ret = ratings_byUser.get(idUser).get(idItem);
        }
        return ret;
    }

    private void addRating(int idUser, int idItem, RatingType rating) {

        if (!items.contains(idItem)) {
            items.add(idItem);
        }

        if (!ratings_byUser.containsKey(idUser)) {
            ratings_byUser.put(idUser, new TreeMap<>());
        }

        ratings_byUser.get(idUser).put(idItem, rating);

        if (rating.ratingValue.floatValue() < rc.min()) {
            rc = new DecimalDomain(rating.ratingValue.floatValue(), rc.max());
        }
        if (rating.ratingValue.floatValue() > rc.max()) {
            rc = new DecimalDomain(rc.min(), rating.ratingValue.floatValue());
        }
    }

    @Override
    public Set<Integer> allUsers() {
        return ratings_byUser.keySet();
    }

    @Override
    public Set<Integer> allRatedItems() {
        return Collections.unmodifiableSet(items);
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) {
        if (ratings_byUser.containsKey(idUser)) {
            return Collections.unmodifiableSet(ratings_byUser.get(idUser).keySet());
        } else {
            return Collections.EMPTY_SET;
        }
    }
    private boolean getItemRated = false;

    @Override
    public Set<Integer> getItemRated(Integer idItem) {
        if (!getItemRated) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getItemRated(Integer idItem):Collection<Integer>]");
            Global.showWarning(ratingDatasetEfficiencyException);
            getItemRated = true;
        }
        return getItemRatingsRated(idItem).keySet();
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) {
        if (ratings_byUser.containsKey(idUser)) {
            return ratings_byUser.get(idUser);
        } else {
            return new TreeMap<>();
        }
    }
    private boolean getItemRatingsRatedWarningMessageShown = false;

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) {
        if (!getItemRatingsRatedWarningMessageShown) {
            RatingDatasetEfficiencyException ratingDatasetEfficiencyException = new RatingDatasetEfficiencyException(this.getClass().getSimpleName() + ": Using an inefficient method:[getItemRatingsRated(Integer idItem):Map<Integer, Byte>]");
            Global.showWarning(ratingDatasetEfficiencyException);
            getItemRatingsRatedWarningMessageShown = true;
        }
        Map<Integer, RatingType> ret = new TreeMap<>();
        for (int idUser : ratings_byUser.keySet()) {
            if (ratings_byUser.get(idUser).containsKey(idItem)) {
                ret.put(idItem, ratings_byUser.get(idUser).get(idItem));
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
