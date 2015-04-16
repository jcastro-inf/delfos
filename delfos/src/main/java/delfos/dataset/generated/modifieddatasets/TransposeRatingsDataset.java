package delfos.dataset.generated.modifieddatasets;

import java.util.Collection;
import java.util.Map;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Implementa la transpuesta de un dataset, cambiando items por productos.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 01-May-2013
 */
public class TransposeRatingsDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final RatingsDataset<RatingType> originalDataset;

    /**
     * Construye el dataset transpuesto.
     *
     * @param ratingsDataset dataset original
     */
    public TransposeRatingsDataset(RatingsDataset<RatingType> ratingsDataset) {
        this.originalDataset = ratingsDataset;

    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        try {
            return originalDataset.getRating(idItem, idUser);
        } catch (UserNotFound ex) {
            throw new ItemNotFound(idItem, ex);
        } catch (ItemNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public Collection<Integer> allUsers() {
        return originalDataset.allRatedItems();
    }

    @Override
    public Collection<Integer> allRatedItems() {
        return originalDataset.allUsers();
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        try {
            return originalDataset.getItemRated(idUser);
        } catch (ItemNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        try {
            return originalDataset.getUserRated(idItem);
        } catch (UserNotFound ex) {
            throw new ItemNotFound(idItem, ex);
        }
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        try {
            return originalDataset.getItemRatingsRated(idUser);
        } catch (ItemNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        try {
            return originalDataset.getUserRatingsRated(idItem);
        } catch (UserNotFound ex) {
            throw new ItemNotFound(idItem, ex);
        }
    }

    @Override
    public Domain getRatingsDomain() {
        return originalDataset.getRatingsDomain();
    }

    @Override
    public int getNumRatings() {
        return originalDataset.getNumRatings();
    }
}
