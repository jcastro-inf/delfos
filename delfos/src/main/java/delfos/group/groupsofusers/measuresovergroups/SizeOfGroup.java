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
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para calcular número de miembros del grupo.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-Junio-2013
 */
public class SizeOfGroup extends GroupMeasureAdapter {
    public SizeOfGroup() {
        super();
    }
    
    /**
     * Devuelve el número de miembros del grupo.
     *
     * @param group Grupo a comprobar.
     * @return número de miembros del grupo.
     */
    @Override
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset {
        return group.size();
    }
}
