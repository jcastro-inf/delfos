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
package delfos.dataset.loaders.epinions;

import delfos.dataset.basic.rating.Rating;

/**
 * Rating del dataset de epinions, con toda la información que el dataset
 * contiene.
 * <p>
 * <p>
 * Original documentation:http://www.trustlet.org/wiki/Extended_Epinions_dataset
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 12-Diciembre-2014
 */
public class EPinionsRating extends Rating {

    /**
     * Valor status del rating del dataset epinions. Si es true, el usuario ha
     * elegido no mostrar su nombre acompañando la valoración. Si es false, al
     * usuario no le importa que sea mostrado su nombre.
     */
    private final Boolean status_HideRating;

    /**
     * Fecha de primera creación de la valoración.
     */
    private final long timestamp_creation;

    /**
     * Fecha de la última modificación de la valoración. Si no se ha modificado
     * la valoración, coincide con el valor de creación.
     */
    private final long timestamp_lastModification;

    /**
     * Vertical id de la valoración.
     */
    private long idVertical;

    public EPinionsRating(Integer idUser, Integer idRated, Number rating) {
        super(idUser, idRated, rating);

        timestamp_creation = -1;
        timestamp_lastModification = -1;
        status_HideRating = false;
    }

    EPinionsRating(long MEMBER_ID, long OBJECT_ID, Number RATING, boolean STATUS, long CREATION, long LAST_MODIFIED, long VERTICAL_ID) {
        super(MEMBER_ID, OBJECT_ID, RATING);

        status_HideRating = STATUS;
        timestamp_creation = CREATION;
        timestamp_lastModification = LAST_MODIFIED;
        idVertical = VERTICAL_ID;
    }

    @Override
    public Rating clone() throws CloneNotSupportedException {
        return new EPinionsRating(getIdUser(), getIdItem(), getRatingValue(), status_HideRating, timestamp_creation, timestamp_lastModification, idVertical);
    }

}
