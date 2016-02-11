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
package delfos.group.groupsofusers.measuresovergroups;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.ParameterOwner;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Define los métodos para solicitar una medida que se aplica sobre un grupo de
 * usuarios.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-Jun-2013
 */
public interface GroupMeasure extends ParameterOwner {

    /**
     * Devuelve una medida asociada al grupo, que sirve para destacar una
     * característica concreta del mismo.
     *
     * @param datasetLoader 
     * @param group
     * @return
     * @throws CannotLoadRatingsDataset
     */
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset;
}
