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

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.ParameterOwner;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;

/**
 * Interfaz que define los métodos de un objeto encargado de cargar conjuntos de
 * datos. Obligatoriamente, todos los {@link DatasetLoader} deben cargar al
 * menos un dataset de valoraciones, que se hará a través del método
 * {@link RatingsDatasetLoader#getRatingsDataset()}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 26-Noviembre-2013
 * @param <RatingType> Tipo de los ratings del dataset de valoraciones.
 */
public interface DatasetLoader<RatingType extends Rating>
        extends ParameterOwner,
        RatingsDatasetLoader<RatingType>,
        Comparable<Object>,
        UsersDatasetLoader,
        ContentDatasetLoader {

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset;

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset;

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset;

}
