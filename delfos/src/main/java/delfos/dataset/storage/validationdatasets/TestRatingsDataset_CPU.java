package delfos.dataset.storage.validationdatasets;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Dataset que deja visibles las valoraciones especificadas en el conjunto de
 * datos indicado.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknow date
 * @version 1.2 06-Mar-2013 Modificación de los parámetros del constructor y
 * corrección de errores.
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @param <RatingType>
 */
public class TestRatingsDataset_CPU<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> implements TestRatingsDataset<RatingType> {

    /**
     * Valoraciones de test. Son las valoraciones que son accesibles.
     */
    private final Map<Integer, Set<Integer>> testRatings_byUser;
    /**
     * Dataset original que contiene el conjunto de datos completo.
     */
    private final RatingsDataset<RatingType> originalDataset;
    /**
     * Buffer para almacenar el conjunto de productos valorados.
     */
    private Set<Integer> allRatedItems;

    public TestRatingsDataset_CPU(RatingsDataset<RatingType> originalDatset, Map<Integer, Set<Integer>> testSet) throws UserNotFound, ItemNotFound {
        super();
        this.originalDataset = originalDatset;

        this.testRatings_byUser = testSet;
        for (int idUser : testSet.keySet()) {
            for (int idItem : testSet.get(idUser)) {
                if (originalDatset.getRating(idUser, idItem) == null) {
                    throw new IllegalArgumentException("Specified rating isn't found in originalDataset");
                }
            }
        }
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        if (testRatings_byUser.containsKey(idUser) && testRatings_byUser.get(idUser).contains(idItem)) {
            return originalDataset.getRating(idUser, idItem);
        } else {
            return null;
        }
    }

    @Override
    public Set<Integer> allUsers() {
        return testRatings_byUser.keySet();
    }

    @Override
    public Set<Integer> allRatedItems() {
        if (this.allRatedItems == null) {
            allRatedItems = new TreeSet<Integer>();

            for (Integer idUser : testRatings_byUser.keySet()) {
                allRatedItems.addAll(testRatings_byUser.get(idUser));
            }
        }

        return Collections.unmodifiableSet(allRatedItems);
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) throws UserNotFound {
        if (testRatings_byUser.containsKey(idUser)) {
            Set<Integer> ret = new TreeSet<Integer>();
            ret.addAll(testRatings_byUser.get(idUser));
            return ret;
        } else {
            throw new UserNotFound(idUser);
        }
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        TreeMap<Integer, RatingType> ret = new TreeMap<Integer, RatingType>();
        Collection<Integer> testSetForUser = testRatings_byUser.get(idUser);
        if (testSetForUser == null) {
            throw new UserNotFound(idUser);
        }
        for (int idItem : testSetForUser) {
            try {
                ret.put(idItem, originalDataset.getRating(idUser, idItem));
            } catch (ItemNotFound ex) {
                Global.showError(ex);
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }
        return ret;
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) {
        Collection<Integer> ret = new LinkedList<Integer>();

        for (int idUser : testRatings_byUser.keySet()) {
            if (testRatings_byUser.get(idUser).contains(idItem)) {
                ret.add(idUser);
            }
        }
        return ret;
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        Map<Integer, RatingType> ret = new TreeMap<Integer, RatingType>();

        for (int idUser : testRatings_byUser.keySet()) {
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
