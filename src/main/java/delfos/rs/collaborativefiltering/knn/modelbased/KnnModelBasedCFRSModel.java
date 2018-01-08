/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.rs.collaborativefiltering.knn.modelbased;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * Modelo de recomendación que utiliza el sistema {@link KnnModelBasedCFRS}.
 * Almacena, para cada item, su lista de items vecinos más cercanos junto con su
 * similitud.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 28-Mayo-2013
 */
public class KnnModelBasedCFRSModel implements Serializable, Iterable<KnnModelItemProfile> {

    private static final long serialVersionUID = 100L;

    /**
     * Perfiles de cada producto.
     */
    private final Map<Long, KnnModelItemProfile> itemsProfiles;

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
    public KnnModelBasedCFRSModel(Map<Long, KnnModelItemProfile> itemsProfiles) {
        this.itemsProfiles = itemsProfiles;
    }

    /**
     * Devuelve el perfil del producto indicado.
     *
     * @param idItem id del producto para el que se recupera el perfil.
     * @return Perfil del producto, null si no hay perfil para el mismo.
     */
    public KnnModelItemProfile getItemProfile(long idItem) {
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
