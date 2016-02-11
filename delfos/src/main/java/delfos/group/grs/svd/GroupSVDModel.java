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
package delfos.group.grs.svd;

import java.io.Serializable;
import java.util.ArrayList;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 08-jul-2013
 */
public class GroupSVDModel implements Serializable {

    private static final long serialVersionUID = 1;
    private final GroupOfUsers group;
    private final ArrayList<Double> groupFeatures;

    public GroupSVDModel(GroupOfUsers group, ArrayList<Double> groupFeatures) {
        this.group = group;
        this.groupFeatures = new ArrayList<Double>(groupFeatures);
    }

    public GroupOfUsers getGroup() {
        return group;
    }

    public ArrayList<Double> getGroupFeatures() {
        return groupFeatures;
    }
}
