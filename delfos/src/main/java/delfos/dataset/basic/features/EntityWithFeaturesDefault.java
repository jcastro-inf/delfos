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
package delfos.dataset.basic.features;

/**
 * Determina los métodos estáticos que una entidad con características necesita
 * consultar.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 28-Enero-2014
 */
public class EntityWithFeaturesDefault {

    private EntityWithFeaturesDefault() {
    }

    /**
     * Comprueba que los vectores son correctos.
     *
     * @param features
     * @param values
     */
    public final static void checkFeatureAndFeatureValuesArrays(Feature[] features, Object[] values) {
        if (features == null) {
            throw new IllegalArgumentException("The feature vector cannot be null.");
        }

        if (values == null) {
            throw new IllegalArgumentException("The feature values vector cannot be null.");
        }

        if (features.length != values.length) {
            throw new IllegalArgumentException("The feature vector cannot have a different lenght than feature values vector "
                    + "(" + features.length + " != " + values.length + ").");
        }

        for (int i = 0; i < features.length; i++) {
            Feature feature = features[i];
            Object value = values[i];

            if (feature == null) {
                throw new IllegalArgumentException("Cannot have a null feature (position=" + i + ").");
            }

            if (value == null) {
                //No pasa nada, se considera que no tiene valor asociado a la característica
            }
        }
    }
}
