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
package delfos.rs.contentbased.vsm.multivalued.entropydependence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.features.Feature;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 14-oct-2013
 */
public class EntropyDependenceCBRSModel extends TreeMap<Integer, EntropyDependenceCBRSItemProfile> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Inter user weights.
     */
    private final Map<Feature, Number> _weights;

    public EntropyDependenceCBRSModel(Map<Integer, EntropyDependenceCBRSItemProfile> items, Map<Feature, Number> weights) {
        super(items);
        this._weights = weights;
    }

    public void getItemProfile(int idItem) {
        if (this.containsKey(idItem)) {
            this.get(idItem);
        } else {
            throw new IllegalArgumentException("The item " + idItem + " has no profile in the model.");
        }
    }

    /**
     * Devuelve la ponderación (inter-user) de la característica indicada.
     *
     * @param feature Característica para la que se busca la ponderación.
     * @return Ponderación de la característica. Si el perfil no contiene la
     * característica, devuelve cero.
     */
    public double getEntropy(Feature feature) {
        if (_weights.containsKey(feature)) {
            return _weights.get(feature).doubleValue();
        } else {
            throw new IllegalArgumentException("The model does not have the feature " + feature);
        }
    }

    protected List<Feature> getAllFeatures() {
        return new ArrayList<Feature>(_weights.keySet());
    }
}
