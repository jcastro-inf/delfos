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
 * Datase que almacena los datos de manera desordenada. Solo calcula los
 * conjuntos de items valorados y usuarios que han valorado. El resto de los
 * mÃ©todos tienen un orden de eficiencia lineal.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.0 Unknow date
 * @param <RatingType>
 */
public class DefaultMemoryRatingsDataset_UserIndexed<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private List<List<RatingType>> ratings;
    private final TreeSet<Integer> items;
    private TreeMap<Integer, Integer> userIndex;
    private DecimalDomain rc;

    /**
     * Crea el dataset vacío.
     */
    public DefaultMemoryRatingsDataset_UserIndexed() {
        super();
        ratings = new ArrayList<>();
        items = new TreeSet<>();
        rc = new DecimalDomain(1, 5);
    }

    public DefaultMemoryRatingsDataset_UserIndexed(Collection<RatingType> ratings) {
        this();
        ratings.stream().forEach((r) -> {
            addRating(r.getIdUser(), r.getIdItem(), r);
        });
    }

    public void setNumUsers(int capacity) {
        List<List<RatingType>> newRatings = new ArrayList<>(capacity);
        ratings.stream().forEach((r) -> {
            newRatings.add(r);
        });
        ratings = newRatings;
    }

    @Override
    public RatingType getRating(int idUser, int idItem) {
        RatingType ret = null;
        Iterator<RatingType> it = ratings.get(userIndex.get(idUser)).listIterator();
        while (it.hasNext() && ret == null) {
            RatingType rating = it.next();
            if (rating.getIdUser() == idUser && rating.getIdItem() == idItem) {
                ret = rating;
            }
        }

        return ret;
    }

    private void addRating(int idUser, int idItem, RatingType rating) {

        if (userIndex == null) {
            userIndex = new TreeMap<>();
            rc = new DecimalDomain(rating.getRatingValue().doubleValue(), rating.getRatingValue().doubleValue());
        }

        if (!userIndex.containsKey(idUser)) {
            ratings.add(new ArrayList<>());
            userIndex.put(idUser, ratings.size() - 1);
        }
        if (!items.contains(idItem)) {
            items.add(idItem);
        }
        ratings.get(userIndex.get(idUser)).add(rating);

        if (rating.getRatingValue().floatValue() < rc.min()) {
            rc = new DecimalDomain(rating.getRatingValue().floatValue(), rc.max());
        }
        if (rating.getRatingValue().floatValue() > rc.max()) {
            rc = new DecimalDomain(rc.min(), rating.getRatingValue().floatValue());
        }
    }

    @Override
    public Set<Integer> allUsers() {
        return userIndex.keySet();
    }

    @Override
    public Set<Integer> allRatedItems() {
        return Collections.unmodifiableSet(items);
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) {
        return getUserRatingsRated(idUser).keySet();
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
        Map<Integer, RatingType> ret = new TreeMap<>();
        Integer index = userIndex.get(idUser);
        if (index == null) {
            return new TreeMap<>();
        }
        List<RatingType> userRow = ratings.get(index);

        for (Iterator<RatingType> it = userRow.listIterator(); it.hasNext();) {
            RatingType rating = it.next();
            if (rating.getIdUser() == idUser) {
                ret.put(rating.getIdItem(), rating);
            }
        }

        return ret;
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
        for (int i = 0; i < userIndex.size(); i++) {
            for (Iterator<RatingType> it = ratings.get(i).listIterator(); it.hasNext();) {
                RatingType rating = it.next();

                if (rating.getIdItem() == idItem) {
                    ret.put(rating.getIdUser(), rating);
                }
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
