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
package delfos.rs.contentbased.vsm.booleanvsm;

import delfos.dataset.basic.features.Feature;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 25-Noviembre-2013
 */
public class FeatureValue implements Comparable<Object> {

    public final Feature feature;
    public final Object value;

    public FeatureValue(Feature feature, Object value) {
        this.feature = feature;
        this.value = value;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof FeatureValue) {
            FeatureValue featureValue = (FeatureValue) o;
            int compareFeatures = this.feature.compareTo(featureValue.feature);
            if (compareFeatures == 0) {
                //Las características son iguales, comparo los valores
                Comparable<Object> thisValue = (Comparable<Object>) this.value;
                Comparable<Object> otherValue = (Comparable<Object>) featureValue.value;

                int compareValues = thisValue.compareTo(otherValue);
                return compareValues;
            } else {
                return compareFeatures;
            }

        } else {
            throw new IllegalArgumentException("Cannot compare with " + o);
        }
    }
}
