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
package delfos.rs.contentbased.vsm.booleanvsm.tfidf;

import java.util.TreeMap;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;

/**
 * Almacena el modelo del sistema {@link TfIdfCBRS}. En su implementación de
 * {@link TreeMap} almacena los perfiles de los productos.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 14-oct-2013
 */
public class TfIdfCBRSModel extends TreeMap<Integer, SparseVector> {

    private static final long serialVersionUID = -3387516993124229948L;

    private final BooleanFeaturesTransformation booleanFeaturesTransformation;
    private SparseVector allIUF;

    public TfIdfCBRSModel(BooleanFeaturesTransformation booleanFeaturesTransformation) {
        this.booleanFeaturesTransformation = booleanFeaturesTransformation;
    }

    public BooleanFeaturesTransformation getBooleanFeaturesTransformation() {
        return booleanFeaturesTransformation;
    }

    public void setAllIuf(SparseVector allIuf) {
        MutableSparseVector aux = booleanFeaturesTransformation.newProfile();
        aux.fill(0);
        aux.add(allIuf);
        this.allIUF = aux;
    }

    public SparseVector getAllIUF() {
        return allIUF;
    }
}
