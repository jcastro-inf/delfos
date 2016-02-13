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
package delfos.dataset.changeable;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;

/**
 * Interfaz de un dataset que permite cambios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 28-Noviembre-2013
 */
public interface ChangeableDatasetLoader extends DatasetLoader<Rating>, ContentDatasetLoader, UsersDatasetLoader {

    /**
     * Obtiene el dataset de contenido que se usará en la recomendación
     *
     * @return dataset de contenido que se usará en la recomendación
     */
    ChangeableContentDataset getChangeableContentDataset() throws CannotLoadContentDataset;

    /**
     * Obtiene el dataset de ratings en memoria que se usará en la
     * recomendación.
     *
     * @return Dataset de ratings.
     */
    ChangeableRatingsDataset getChangeableRatingsDataset() throws CannotLoadRatingsDataset;

    ChangeableUsersDataset getChangeableUsersDataset() throws CannotLoadUsersDataset;

    /**
     * Crea las estructuras necesarias para almacenar en persistencia el
     * dataset. Si ya existía, se debe lanzar un error.
     *
     * @throws IllegalStateException Cuando la base de datos ya estaba
     * inicializada.
     */
    public void initStructures();

    public void commitChangesInPersistence();
}
