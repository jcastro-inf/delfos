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
package delfos.dataset.basic.features;

import java.util.Set;

/**
 * Determina los métodos que una entidad con características, como un usuario o
 * un producto, debe implementar. Generaliza el anterior comportamiento de los
 * productos y el manejo del contenido de los mismos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 18-sep-2013
 */
public interface EntityWithFeatures {

    /**
     * Devuelve el valor que la entidad tiene para una característica dado
     *
     * @param feature característica que se desea consultar
     * @return devuelve un objeto con el valor de la característica. Si la
     * característica es nominal, es de tipo <code>{@link String}</code>; si es
     * numérico, devuelve un <code>{@link Float}</code>
     */
    public Object getFeatureValue(Feature feature);

    /**
     * Devuelve las características que están definidas para este producto.
     *
     * @return conjunto de características de la entidad
     */
    public Set<Feature> getFeatures();

    /**
     * Devuelve el identificador de la entidad al que pertenece el contenido
     * almacenado en este objeto.
     *
     * @return identificador de la entidad
     */
    public Integer getId();

    /**
     * Devuelve el nombre de la entidad.
     *
     * @return Nombre de la entidad.
     */
    public String getName();

    @Override
    public String toString();
}
