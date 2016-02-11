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
import java.util.Collections;
import java.util.List;

/**
 * Modelo de recomendación del sistema {@link MeanRatingRS}, que almacena para
 * cada producto su valoración media.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 *
 * @version 1.0 9-Junio-2013
 */
public class MeanRatingRSModel implements Serializable {

    private static final long serialVersionUID = 301;
    private final List<MeanRating> rangedMeanRatings;

    public MeanRatingRSModel(List<MeanRating> rangedMeanRatings) {
        Collections.sort(rangedMeanRatings);
        this.rangedMeanRatings = rangedMeanRatings;
    }

    /**
     * Devuelve la lista de productos, ordenada por su valoración media.
     *
     * @return Modelo de recomendación.
     */
    public List<MeanRating> getRangedMeanRatings() {
        return Collections.unmodifiableList(rangedMeanRatings);
    }
}
