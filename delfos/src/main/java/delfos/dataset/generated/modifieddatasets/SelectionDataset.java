package delfos.dataset.generated.modifieddatasets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
 * Clase que se comporta como una envoltura de un dataset, haciendo visibles
 * solo los productos y los usuarios que se le pasan por parámetros.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.0 Unknow date
 * @param <RatingType>
 */
public class SelectionDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private Collection<Integer> usuariosPermitidos = new TreeSet<>();
    private Collection<Integer> productosPermitidos = new TreeSet<>();
    private RatingsDataset<RatingType> originalDataset;

    public SelectionDataset(RatingsDataset<RatingType> ratingsDataset) {
        super();
        this.originalDataset = ratingsDataset;
    }

    public void setOriginalDataset(RatingsDataset<RatingType> originalDataset) {
        this.originalDataset = originalDataset;
        usuariosPermitidos = new TreeSet<>();
        productosPermitidos = new TreeSet<>();
    }

    public void setProductosPermitidos(Collection<Integer> productosPermitidos) {
        //Comprobar si el conjunto de productos existe en el dataset
        TreeSet<Integer> productosPermitidosPrev = new TreeSet<>(productosPermitidos);
        productosPermitidosPrev.removeAll(originalDataset.allRatedItems());

        productosPermitidosPrev.parallelStream().forEach((idItem) -> {
            Global.showWarning("Item " + idItem + " not in original dataset");
        });

        this.productosPermitidos = productosPermitidos;
    }

    public void setUsuariosPermitidos(Collection<Integer> usuariosPermitidos) {
        //Comprobar si el conjunto de usuarios existe en el dataset
        for (Integer idUser : usuariosPermitidos) {
            if (!originalDataset.allUsers().contains(idUser)) {
                Global.showWarning("User " + idUser + " not in original dataset");
            }
        }

        this.usuariosPermitidos = usuariosPermitidos;
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        if (!usuariosPermitidos.contains(idUser)) {
            throw new UserNotFound(idUser);
        }
        if (!productosPermitidos.contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
        return originalDataset.getRating(idUser, idItem);
    }

    @Override
    public Collection<Integer> allUsers() {
        Collection<Integer> ratedUsers = new TreeSet<>();
        usuariosPermitidos.stream().forEach((idUser) -> {
            try {
                if (!getUserRated(idUser).isEmpty()) {
                    ratedUsers.add(idUser);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        });
        return ratedUsers;
    }

    @Override
    public Collection<Integer> allRatedItems() {
        Collection<Integer> ratedItems = new TreeSet<>();
        for (int idItem : productosPermitidos) {
            try {
                if (isRatedItem(idItem)) {
                    ratedItems.add(idItem);
                }
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }
        return ratedItems;
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        if (!usuariosPermitidos.contains(idUser)) {
            throw new UserNotFound(idUser);
        }
        TreeSet<Integer> userRated = new TreeSet<>(originalDataset.getUserRated(idUser));
        userRated.retainAll(productosPermitidos);
        return userRated;
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        if (!usuariosPermitidos.contains(idUser)) {
            throw new UserNotFound(idUser);
        }

        Map<Integer, RatingType> userRatingsRated = new TreeMap<>(originalDataset.getUserRatingsRated(idUser));
        for (Iterator<Entry<Integer, RatingType>> it = userRatingsRated.entrySet().iterator(); it.hasNext();) {
            Entry<Integer, RatingType> entry = it.next();
            if (!productosPermitidos.contains(entry.getKey())) {
                it.remove();
            }
        }
        return userRatingsRated;
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        if (!productosPermitidos.contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
        TreeSet<Integer> itemRatingsRated = new TreeSet<>(originalDataset.getItemRated(idItem));
        itemRatingsRated.retainAll(usuariosPermitidos);
        return itemRatingsRated;
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        if (!productosPermitidos.contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
        Map<Integer, RatingType> itemRatingsRated = new TreeMap<>(originalDataset.getItemRatingsRated(idItem));
        for (Iterator<Entry<Integer, RatingType>> it = itemRatingsRated.entrySet().iterator(); it.hasNext();) {
            Entry<Integer, RatingType> entry = it.next();
            if (!usuariosPermitidos.contains(entry.getKey())) {
                it.remove();
            }
        }
        return itemRatingsRated;
    }

    @Override
    public Domain getRatingsDomain() {
        return originalDataset.getRatingsDomain();
    }

    @Override
    public int getNumRatings() {
        int size = 0;
        for (int idUser : usuariosPermitidos) {
            try {
                size += getUserRated(idUser).size();
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        return size;
    }

    @Override
    public boolean isRatedItem(int idItem) throws ItemNotFound {
        Collection<Integer> itemRated = new TreeSet<>(originalDataset.getItemRated(idItem));
        itemRated.retainAll(usuariosPermitidos);
        return !itemRated.isEmpty();
    }

    @Override
    public int sizeOfItemRatings(int idItem) throws ItemNotFound {
        Collection<Integer> itemRated = new TreeSet<>(originalDataset.getItemRated(idItem));
        itemRated.retainAll(usuariosPermitidos);
        return itemRated.size();
    }

    @Override
    public boolean isRatedUser(int idUser) throws UserNotFound {
        Collection<Integer> userRated = new TreeSet<>(originalDataset.getUserRated(idUser));
        userRated.retainAll(productosPermitidos);
        return !userRated.isEmpty();
    }

    @Override
    public int sizeOfUserRatings(int idUser) throws UserNotFound {
        Collection<Integer> userRated = new TreeSet<>(originalDataset.getUserRated(idUser));
        userRated.retainAll(productosPermitidos);
        return userRated.size();
    }
}
