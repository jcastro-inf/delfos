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
package delfos.rs.contentbased.vsm.booleanvsm.profile.item;

import java.io.Serializable;
import java.util.Set;
import delfos.dataset.basic.features.Feature;
import delfos.rs.ItemProfile;

/**
 * Interfaz que establece los métodos de un perfil de usuario para sistemas de
 * recomendación basados en contenido.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.1 9-Octubre-2013
 */
public interface BooleanItemProfile extends ItemProfile, Serializable {

    /**
     * Obtiene el valor de una característica determinada. El parámetro
     * <code>value</code> de la característica es ignorado si la característica
     * tiene el mismo peso independientemente de su valor
     *
     * @param f Característica para la que se desea conocer el peso
     * @param featureValue Valor de la característica para la que se desea
     * conocer el peso. Es ignorado si una característica tiene el mismo peso
     * independientemente de su valor
     * @return valor de la caracteristica en el perfil
     */
    public float getFeatureValueValue(Feature f, Object featureValue);

    /**
     * Devuelve los valores de características valorados por el usuario al que
     * pertenece este perfil
     *
     * @param f Característica para la que se desea conocer los valores
     * valorados
     * @return Conjunto de valores de la característica
     */
    public Set<Object> getValuedFeatureValues(Feature f);

    /**
     * Devuelve true si el perfil de usuario tiene valorado el valor
     * <code>value</code> de la característica
     * <code>f</code>.
     *
     * @param f Posible característica de un item.
     * @param value Valor de la característica f.
     * @return true si el perfil de usuario tiene valores para el par
     * (característica,valor)
     */
    public boolean contains(Feature f, Object value);

    /**
     * Devuelve las características que han sido valoradas por el usuario al que
     * pertenece este perfil
     *
     * @return Conjunto de características valoradas
     */
    public Iterable<Feature> getFeatures();

    /**
     * Método a implementar para liberar los recursos de este perfil de usuario.
     */
    public void cleanProfile();
}
