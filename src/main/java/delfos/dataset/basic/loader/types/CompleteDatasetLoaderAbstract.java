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
package delfos.dataset.basic.loader.types;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.dataset.basic.rating.Rating;

/**
 * Establece las operaciones que un <code>DatasetLoader</code> debe implementar.
 * Un <code>DatasetLoader</code> se encarga de cargar un dataset de
 * recomendación para su posterior uso con un sistema de recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 Unknown date.
 * @version 1.0.1 26-Mayo-2013
 * @version 1.0.2 15-Noviembre-2013
 * @param <RatingType>
 */
public abstract class CompleteDatasetLoaderAbstract<RatingType extends Rating> extends DatasetLoaderAbstract<RatingType> implements Comparable<Object>, DatasetLoader<RatingType>, ContentDatasetLoader, UsersDatasetLoader {

    @Override
    public int compareTo(Object o) {
        if (o instanceof DatasetLoader) {
            DatasetLoader parameterOwner = (DatasetLoader) o;
            return ParameterOwnerAdapter.compare(this, parameterOwner);
        }

        throw new IllegalArgumentException("The type is not valid, must be a " + DatasetLoader.class);
    }
}
