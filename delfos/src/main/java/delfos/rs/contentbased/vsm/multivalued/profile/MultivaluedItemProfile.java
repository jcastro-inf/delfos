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
package delfos.rs.contentbased.vsm.multivalued.profile;

import java.io.Serializable;
import delfos.dataset.basic.features.Feature;
import delfos.rs.ItemProfile;

/**
 * Define los métodos de un perfil de producto multivaluado para sistemas de
 * recomendación basados en contenido.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 14-Octubre-2013
 */
public interface MultivaluedItemProfile extends Serializable, ItemProfile {

    /**
     * Devuelve el Id del producto al que se refiere este perfil.
     *
     * @return Id del producto.
     */
    @Override
    public int getId();

    /**
     * Devuelve las características definidas para este perfil de producto.
     *
     * @return Características.
     */
    public Iterable<Feature> getFeatures();

    /**
     * Devuelve el valor para la característica indicada.
     *
     * @param itemFeature Característica que se busca.
     * @return Valor de la característica indicada.
     */
    public Object getFeatureValue(Feature itemFeature);
}
