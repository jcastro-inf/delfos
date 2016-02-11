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
package delfos.rs.nonpersonalised.meanrating.arithmeticmean;

import java.io.Serializable;

/**
 * Clase para almacenar el modelo del sistema de recomendación basado en
 * valoración media de los productos. Almacena un producto y su valoración
 * media.
 *
 * @see MeanRatingRS
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 21-Feb-2013
 */
public class MeanRating implements Serializable, Comparable<MeanRating> {
    
    private static final long serialVersionUID = 103L;

    /**
     * Id del producto.
     */
    private int _idItem;
    /**
     * Valor de preferencia medio del producto.
     */
    private Number _preference;

    
    /**
     * Se implementa el constructor por defecto para que el objeto sea
     * serializable.
     */
    protected MeanRating() {
    }
    

    /**
     * Constructor que asigna los valores del objeto.
     *
     * @param idItem Producto sobre el que se realiza la recomendación
     * @param preference Valor de preferencia medio del producto.
     */
    public MeanRating(Integer idItem, Number preference) {
        this._idItem = idItem;
        this._preference = preference;
    }

    /**
     * ID del producto al que se refiere este objeto.
     *
     * @return ID del producto al que se refiere este objeto.
     */
    public int getIdItem() {
        return _idItem;
    }

    /**
     * Valor de preferencia medio del producto. Cuanto mayor es, más probable es
     * que el producto sea relevante para un usuario.
     *
     * @return Valoración media del producto.
     */
    public Number getPreference() {
        return _preference;
    }

    @Override
    public String toString() {
        return "item:" + _idItem + "-->" + _preference;
    }

    /**
     * Compara este objeto con otro del mismo tipo, teniendo en cuenta el valor
     * de preferencia medio de cada uno. Los ordena en orden descendente.
     *
     * {@inheritDoc }
     */
    @Override
    public int compareTo(MeanRating o) {
        float diff = _preference.floatValue() - o._preference.floatValue();
        if (diff == 0) {
            return 0;
        } else if (diff > 0) {
            return -1;
        } else {
            return 1;
        }
    }
}
