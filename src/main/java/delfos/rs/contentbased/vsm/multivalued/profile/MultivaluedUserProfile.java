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
import java.util.Set;
import delfos.dataset.basic.features.Feature;

/**
 * Define los métodos de un perfil de usuario multivaluado para sistemas de
 * recomendación basados en contenido.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 01-Mar-2013
 */
public interface MultivaluedUserProfile extends Serializable {

    /**
     * Devuelve el Id del usuario al que se refiere este perfil.
     *
     * @return Id del usuario.
     */
    public int getId();

    /**
     * Devuelve las características definidas para este perfil de usuario.
     *
     * @return Características.
     */
    public Iterable<Feature> getFeatures();

    /**
     * Comprueba si el perfil contiene el valor de la característica indicados.
     *
     * @param feature Característica que se busca
     * @param featureValue Valor de la característica que se busca.
     * @return true si contiene el valor de la característica, false si no lo
     * contiene.
     */
    public boolean contains(Feature feature, Object featureValue);

    /**
     * Devuelve el valor del perfil para el valor concreto de la característica.
     *
     * @param feature Característica para la que se busca el valor.
     * @param featureValue Valor de la característica para el que se busca el
     * balor.
     * @return Valor del perfil para el valor y la característica indicados.
     *
     * @throws IllegalArgumentException Si no encuentra la característica o el
     * valor de la misma.
     */
    public float getFeatureValueValue(Feature feature, Object featureValue);

    /**
     * Devuelve la ponderación de la característica indicada.
     *
     * @param feature Característica para la que se busca la ponderación.
     * @return Ponderación de la característica. Si el perfil no contiene la
     * característica, devuelve cero.
     */
    public float getFeatureValueWeight(Feature feature);

    /**
     * Devuelve los valores de la característica especificada definidos en este
     * perfil de usuario.
     *
     * @param feature Característica para los que se busca sus valores.
     * @return Conjunto de valores de la característica. Si no contiene la
     * característica, devuelve null.
     */
    public Set<Object> getValuedFeatureValues(Feature feature);
}
