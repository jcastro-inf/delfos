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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.features.Feature;
import delfos.rs.contentbased.vsm.booleanvsm.profile.BooleanUserProfile;

/**
 * Perfil de usuario para el sistema de recomendación {@link TfIdfCBRS}. Es un
 * perfil booleano con vector de características y vector de ponderación de las
 * características.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 30-oct-2013
 */
public class TfIdfCBRSUserProfile implements BooleanUserProfile {

    private static final long serialVersionUID = 112L;

    /**
     * Id del usuario al que se refiere este perfil.
     */
    private final long _idUser;
    /**
     * Valor del perfil para cada valor de cada característica.
     */
    private Map<Feature, Map<Object, Double>> _values;
    /**
     * Ponderación de cada valor de cada característica.
     */
    private Map<Feature, Map<Object, Double>> _weights;

    /**
     * Constructor que genera un perfil de usuario con ponderación de
     * características a partir de los valores previamente calculados.
     *
     * @param idUser Id del usuario al que representa este perfil.
     * @param userProfileValues Valor que el perfil de usuario tiene para cada
     * una de los características posibles en el dataset de contenido.
     * @param userProfileWeights Importancia de cada una de los características
     * para el usuario.
     */
    public TfIdfCBRSUserProfile(long idUser, Map<Feature, Map<Object, Double>> userProfileValues, Map<Feature, Map<Object, Double>> userProfileWeights) {
        this._idUser = idUser;
        this._values = userProfileValues;
        this._weights = userProfileWeights;
    }

    @Override
    public double getFeatureValueWeight(Feature f, Object featureValue) {
        if (_weights.containsKey(f)) {
            if (_weights.get(f).containsKey(featureValue)) {
                return _weights.get(f).get(featureValue);
            } else {
                throw new IllegalArgumentException("No se tiene ponderación para el valor '" + featureValue + "' de la característica " + f + ".");
            }
        } else {
            throw new IllegalArgumentException("No se tiene ponderación para la característica.");
        }

    }

    @Override
    public double getFeatureValueValue(Feature f, Object featureValue) {
        return _values.get(f).get(featureValue);
    }

    @Override
    public Collection<Feature> getFeatures() {
        return _values.keySet();
    }

    @Override
    public Set<Object> getValuedFeatureValues(Feature f) {
        Set<Object> ret = new TreeSet<Object>();
        for (Object s : _values.get(f).keySet()) {
            ret.add(s);
        }
        return ret;
    }

    @Override
    public boolean contains(Feature f, Object value) {
        return _values.containsKey(f) && _values.get(f).containsKey(value);
    }

    @Override
    public long getId() {
        return _idUser;
    }

    @Override
    public void cleanProfile() {
        for (Feature key : _values.keySet()) {
            _values.get(key).clear();
        }
        _values.clear();
        _values = new TreeMap<Feature, Map<Object, Double>>();

        for (Feature key : _weights.keySet()) {
            _weights.get(key).clear();
        }
        _weights.clear();
        _weights = new TreeMap<Feature, Map<Object, Double>>();
    }
}
