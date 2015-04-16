package delfos.rs.collaborativefiltering.knn.modelbased;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * Modelo de recomendación que utiliza el sistema {@link KnnModelBasedCFRS}.
 * Almacena, para cada item, su lista de items vecinos más cercanos junto con su
 * similitud.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 *
 * @version 1.0 28-Mayo-2013
 */
public class KnnModelBasedCFRSModel implements Serializable, Iterable<KnnModelItemProfile> {

    private static final long serialVersionUID = 100L;

    /**
     * Perfiles de cada producto.
     */
    private final Map<Integer, KnnModelItemProfile> itemsProfiles;

    /**
     * Se implementa el constructor por defecto para que el objeto sea
     * serializable.
     */
    protected KnnModelBasedCFRSModel() {
        itemsProfiles = null;
    }

    /**
     * Crea el modelo de recomendación con los perfiles indicados.
     *
     * @param itemsProfiles Perfiles del modelo de recomendación.
     */
    public KnnModelBasedCFRSModel(Map<Integer, KnnModelItemProfile> itemsProfiles) {
        this.itemsProfiles = itemsProfiles;
    }

    /**
     * Devuelve el perfil del producto indicado.
     *
     * @param idItem id del producto para el que se recupera el perfil.
     * @return Perfil del producto, null si no hay perfil para el mismo.
     */
    public KnnModelItemProfile getItemProfile(int idItem) {
        return itemsProfiles.get(idItem);
    }

    @Override
    public Iterator<KnnModelItemProfile> iterator() {
        return itemsProfiles.values().iterator();
    }

    /**
     * Devuelve el número de perfiles de producto que tiene este modelo de
     * recomendación.
     *
     * @return Número de perfiles de producto que tiene este modelo de
     * recomendación.
     */
    public int getNumProfiles() {
        return itemsProfiles.size();
    }
}
