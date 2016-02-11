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
package delfos.group.grs.penalty.grouper;

import java.util.Collection;
import java.util.Set;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 *
 * @version 18-sep-2014
* @author Jorge Castro Gallardo
 */
public abstract class Grouper extends ParameterOwnerAdapter {

    public abstract Collection<Collection<Integer>> groupUsers(
            RatingsDataset<? extends Rating> ratings,
            Set<Integer> users);

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUPER;
    }
}
