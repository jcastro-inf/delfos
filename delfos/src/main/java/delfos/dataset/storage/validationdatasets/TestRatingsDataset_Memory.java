package delfos.dataset.storage.validationdatasets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
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
public class TestRatingsDataset_Memory<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> implements TestRatingsDataset<RatingType> {

    private final BothIndexRatingsDataset<RatingType> testRatingsDataset;
    /**
     * Dataset original que contiene el conjunto de datos completo.
     */
    private final RatingsDataset<RatingType> originalRatingsDataset;

    public TestRatingsDataset_Memory(RatingsDataset<RatingType> originalRatingsDataset, Map<Integer, Set<Integer>> testSet) throws UserNotFound, ItemNotFound {
        super();

        checkParameters(testSet, originalRatingsDataset);
        this.originalRatingsDataset = originalRatingsDataset;

        List<RatingType> testRatings = new ArrayList<>();
        for (RatingType rating : originalRatingsDataset) {
            final int idUser = rating.idUser;
            final int idItem = rating.idItem;
            if (testSet.containsKey(idUser) && testSet.get(idUser).contains(idItem)) {
                //Este rating no está en el testSet, se añade.
                testRatings.add(rating);
            } else {
                //Este rating está en el testSet, no se añade.

            }
        }

        testRatingsDataset = new BothIndexRatingsDataset<>(testRatings);
    }

    public final void checkParameters(Map<Integer, Set<Integer>> testSet, RatingsDataset<RatingType> originalDatset) throws UserNotFound, IllegalArgumentException, ItemNotFound {
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
        return testRatingsDataset.getRating(idUser, idItem);
    }

    @Override
    public Collection<Integer> allUsers() {
        return testRatingsDataset.allUsers();
    }

    @Override
    public Collection<Integer> allRatedItems() {
        return testRatingsDataset.allRatedItems();
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        return testRatingsDataset.getUserRated(idUser);
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        return testRatingsDataset.getUserRatingsRated(idUser);
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        return testRatingsDataset.getItemRated(idItem);
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        return testRatingsDataset.getItemRatingsRated(idItem);
    }

    @Override
    public Domain getRatingsDomain() {
        return testRatingsDataset.getRatingsDomain();
    }

    @Override
    public RatingsDataset<RatingType> getOriginalDataset() {
        return originalRatingsDataset;
    }
}
