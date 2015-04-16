package delfos.dataset.generated.modifieddatasets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.ERROR_CODES;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Este dataset se usa para eliminar valoraciones concretas de un dataset de
 * partida. Se usa generalmente para ocultar los datos que se desean predecir
 * con un sistema de recomendación, es decir, se utiliza para generar datasets
 * de entrenamiento
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 11-12-2012
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @deprecated Se ha demostrado que este dataset de valoraciones es muy lento,
 * debido a problemas de escalabilidad.
 */
@Deprecated
public class RemoveRatingsDatasets<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    /**
     * Ratings ocultados del dataset original indexados por usuarios
     */
    private Map<Integer, Set<Integer>> eliminadosByUser = new TreeMap<Integer, Set<Integer>>();
    /**
     * Ratings ocultados del dataset original indexados por items
     */
    private Map<Integer, Set<Integer>> eliminadosByItem = new TreeMap<Integer, Set<Integer>>();
    /**
     * Dataset original del que se eliminan las valoraciones
     */
    private final RatingsDataset<RatingType> originalDataset;

    /**
     * Crea un dataset por defecto a partir de otro. El dataset generado será
     * idéntico al dataset original hasta que se use el método
     * {@link RemoveRatingsDatasets#removeRating(delfos.Dataset.Rating) }
     * o
     * {@link RemoveRatingsDatasets#removeRating(int, int)}.
     *
     * @param originalDataset Dataset original, que contiene todass las
     * valoraciones
     */
    public RemoveRatingsDatasets(RatingsDataset<RatingType> originalDataset) {
        super();
        this.originalDataset = originalDataset;
    }

    /**
     * Indica a este dataset que oculte el ratingValue indicado por parámetro.
     * Después de invocar este método, los métodos de consulta de este objeto
     * nunca devolverán esta valoración (lo que significa que en este dataset,
     * el usuario no ha valorado el producto)
     *
     * @param ratingValue Valoración que se desea ocultar
     */
    public void removeRating(Rating rating) {
        removeRating(rating.idUser, rating.idItem);
    }

    /**
     * Indica a este dataset que oculte el ratingValue indicado por parámetro.
     * Después de invocar este método, los métodos de consulta de este objeto
     * nunca devolverán esta valoración (lo que significa que en este dataset,
     * el usuario no ha valorado el producto)
     *
     * @param idUser Id del usuario para el que se quiere ocultar una valoración
     * @param idItem Id del producto que se oculta
     */
    public void removeRating(int idUser, int idItem) {
        if (!eliminadosByUser.containsKey(idUser)) {
            eliminadosByUser.put(idUser, new TreeSet<Integer>());
        }
        eliminadosByUser.get(idUser).add(idItem);

        if (!eliminadosByItem.containsKey(idItem)) {
            eliminadosByItem.put(idItem, new TreeSet<Integer>());
        }
        eliminadosByItem.get(idItem).add(idUser);
    }

    /**
     * Reinicia el dataset. Una vez llamado este método, el dataset original y
     * este dataset serán idénticos
     */
    public void clear() {
        for (Entry<Integer, Set<Integer>> e : eliminadosByUser.entrySet()) {
            e.getValue().clear();
        }
        eliminadosByUser.clear();
        for (Entry<Integer, Set<Integer>> e : eliminadosByItem.entrySet()) {
            e.getValue().clear();
        }
        eliminadosByItem.clear();
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        if (eliminadosByUser.containsKey(idUser) && eliminadosByUser.get(idUser).contains(idItem)) {
            return null;
        } else {
            return originalDataset.getRating(idUser, idItem);
        }
    }

    @Override
    public Collection<Integer> allUsers() {
        Collection<Integer> ret = new TreeSet<Integer>(originalDataset.allUsers());
        if (!eliminadosByUser.isEmpty()) {
            for (Iterator<Integer> it = ret.iterator(); it.hasNext();) {
                int idUser = it.next();
                if (eliminadosByUser.containsKey(idUser)) {
                    try {
                        if (originalDataset.sizeOfUserRatings(idUser) - eliminadosByUser.get(idUser).size() == 0) {
                            it.remove();
                        }
                    } catch (UserNotFound ex) {
                        Global.showError(ex);
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public Collection<Integer> allRatedItems() {
        Collection<Integer> ret = new TreeSet<Integer>(originalDataset.allRatedItems());
        if (!eliminadosByItem.isEmpty()) {
            for (Iterator<Integer> it = ret.iterator(); it.hasNext();) {
                Integer idItem = it.next();
                if (eliminadosByItem.containsKey(idItem)) {
                    try {
                        if (originalDataset.sizeOfItemRatings(idItem) - eliminadosByItem.get(idItem).size() == 0) {
                            it.remove();
                        }
                    } catch (ItemNotFound ex) {
                        Global.showError(ex);
                        ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        if (eliminadosByUser.containsKey(idUser)) {
            //Elimina las que pertenezcan
            Collection<Integer> ret = new TreeSet<Integer>(originalDataset.getUserRated(idUser));
            for (Iterator<Integer> it = ret.iterator(); it.hasNext();) {
                Integer idItem = it.next();
                if (eliminadosByUser.get(idUser).contains(idItem)) {
                    it.remove();
                }
            }
            return ret;
        } else {
            return originalDataset.getUserRated(idUser);
        }
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        if (eliminadosByItem.containsKey(idItem)) {
            //Elimina las que pertenezcan
            Collection<Integer> ret = new TreeSet<Integer>(originalDataset.getItemRated(idItem));
            for (Iterator<Integer> it = ret.iterator(); it.hasNext();) {
                Integer idUser = it.next();
                if (eliminadosByItem.get(idItem).contains(idUser)) {
                    it.remove();
                }
            }
            return ret;
        } else {
            return originalDataset.getItemRated(idItem);
        }
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        if (eliminadosByUser.containsKey(idUser)) {
            //Elimina las que pertenezcan
            Map<Integer, RatingType> ret = new TreeMap<Integer, RatingType>(originalDataset.getUserRatingsRated(idUser));
            for (Iterator<Integer> it = ret.keySet().iterator(); it.hasNext();) {
                int idItem = it.next();
                if (eliminadosByUser.get(idUser).contains(idItem)) {
                    it.remove();
                }
            }
            return ret;
        } else {
            return originalDataset.getUserRatingsRated(idUser);
        }
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        if (eliminadosByItem.containsKey(idItem)) {
            //Elimina las que pertenezcan
            Map<Integer, RatingType> ret = new TreeMap<Integer, RatingType>(originalDataset.getItemRatingsRated(idItem));
            for (Iterator<Integer> it = ret.keySet().iterator(); it.hasNext();) {
                int idUser = it.next();
                if (eliminadosByItem.get(idItem).contains(idUser)) {
                    it.remove();
                }
            }
            return ret;
        } else {
            return originalDataset.getItemRatingsRated(idItem);
        }
    }

    @Override
    public Domain getRatingsDomain() {
        return originalDataset.getRatingsDomain();
    }

    /**
     * Elimina las valoraciones de un usuario concreto sobre un conjunto de
     * productos.
     *
     * @param idUser Usuario del que se borran las valoraciones.
     * @param idItemList Conjunto de productos.
     */
    public void removeRatings(int idUser, Collection<Integer> idItemList) {
        for (int idItem : idItemList) {
            removeRating(idUser, idItem);
        }
    }
}
