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
package delfos.rs.contentbased.vsm.booleanvsm.basic;

import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.contentbased.vsm.booleanvsm.SparseVector;
import java.util.TreeMap;

/**
 * Almacena el modelo del sistema {@link BasicBooleanCBRS}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 09-oct-2013
 */
public class BasicBooleanCBRSModel extends TreeMap<Long, SparseVector<Long>> {

    private static final long serialVersionUID = 1L;
    protected BooleanFeaturesTransformation booleanFeaturesTransformation;

    public BasicBooleanCBRSModel(BooleanFeaturesTransformation booleanFeaturesTransformation) {
        this.booleanFeaturesTransformation = booleanFeaturesTransformation;
    }

    public BooleanFeaturesTransformation getBooleanFeaturesTransformation() {
        return booleanFeaturesTransformation;
    }
}
