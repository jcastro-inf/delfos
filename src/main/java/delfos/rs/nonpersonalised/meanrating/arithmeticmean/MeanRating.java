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

import delfos.dataset.basic.item.Item;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Clase para almacenar el modelo del sistema de recomendación basado en
 * valoración media de los productos. Almacena un producto y su valoración
 * media.
 *
 * @see MeanRatingRS
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class MeanRating implements Serializable, Comparable<MeanRating> {

    private static final long serialVersionUID = 103L;
    public static final Comparator<MeanRating> BY_PREFERENCE_DESC
            = (o1, o2) -> -Double.compare(o1.preference, o2.preference);

    /**
     * Id del producto.
     */
    private Item item;
    /**
     * Valor de preferencia medio del producto.
     */
    private Double preference;

    /**
     * Se implementa el constructor por defecto para que el objeto sea
     * serializable.
     */
    protected MeanRating() {
    }

    public MeanRating(Item item, Double preference) {
        this.item = item;
        this.preference = preference;
    }

    /**
     * Producto al que se refiere este objeto.
     *
     * @return Producto al que se refiere este objeto.
     */
    public Item getItem() {
        return item;
    }

    /**
     * Valor de preferencia medio del producto. Cuanto mayor es, más probable es
     * que el producto sea relevante para un usuario.
     *
     * @return Valoración media del producto.
     */
    public double getPreference() {
        return preference;
    }

    @Override
    public String toString() {
        return "item:" + item.getId() + "-->" + preference + " '" + item.getName() + "'";
    }

    /**
     * Compara este objeto con otro del mismo tipo, teniendo en cuenta el valor
     * de preferencia medio de cada uno. Los ordena en orden descendente.
     *
     * {@inheritDoc }
     */
    @Override
    public int compareTo(MeanRating o) {
        return MeanRating.BY_PREFERENCE_DESC.compare(this, o);
    }
}
