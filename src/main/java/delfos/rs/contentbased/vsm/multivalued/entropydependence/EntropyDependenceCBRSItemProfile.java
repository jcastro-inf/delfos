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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.features.Feature;
import delfos.rs.contentbased.vsm.multivalued.profile.MultivaluedItemProfile;

/**
 * Almacena el perfil de un producto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 14-oct-2013
 */
public class EntropyDependenceCBRSItemProfile implements MultivaluedItemProfile {

    private static final long serialVersionUID = 1L;
    private final long idItem;
    private final Map<Feature, Object> featureValues;

    private EntropyDependenceCBRSItemProfile() {
        idItem = Long.MIN_VALUE;
        featureValues = null;
    }

    public EntropyDependenceCBRSItemProfile(long idItem, Map<Feature, Object> featureValue) {
        this.featureValues = new TreeMap<Feature, Object>(featureValue);
        this.idItem = idItem;
    }

    @Override
    public long getId() {
        return idItem;
    }

    @Override
    public Iterable<Feature> getFeatures() {
        return new ArrayList<Feature>(featureValues.keySet());
    }

    @Override
    public Object getFeatureValue(Feature feature) {
        if (featureValues.containsKey(feature)) {
            return featureValues.get(feature);
        } else {
            throw new IllegalArgumentException("The item '" + idItem + "' does not have the feature " + feature);
        }
    }

    @Override
    public String toString() {
        return featureValues.toString();
    }
}
