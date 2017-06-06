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
package delfos.rs.contentbased.vsm.booleanvsm.profile.item;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.features.Feature;

/**
 * Clase que implementa el perfil de usuario para los sistemas de recomendación
 * basados en contenido que utilizan un espacio vectorial para representar los
 * usuarios y productos. En este tipo de perfil se considera cada par
 * (característica,valor) como una característica diferente, por lo que al
 * especificar los valores de los características y los pesos de los mismos para
 * el usuario se especifica como un mapa de mapas de valores en el que el primer
 * mapa representa la característica (la clave es un {@link Feature}) y el
 * segundo es un mapa que contiene el valor de la característica (la clave es un
 * {@link String}, si es la característica es numérico se convierte a cadena) y
 * el número asignado al mismo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 9-Octubre-2013
 */
public class BooleanItemProfileStatic implements BooleanItemProfile, Serializable {

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
     * @param values Valor que el perfil de usuario tiene para cada una de los
     * características posibles en el dataset de contenido.
     * @param weights Importancia de cada una de los características para el
     * usuario.
     */
    public BooleanItemProfileStatic(int idUser, Map<Feature, Map<Object, Double>> values, Map<Feature, Map<Object, Double>> weights) {
        this._idUser = idUser;
        this._values = values;
        this._weights = weights;
    }

    /**
     * Constructor que genera un perfil de usuario sin ponderación de
     * características a partir de los valores previamente calculados
     *
     * @param idUser Idel usuario al que representa este perfil
     * @param values Valor que el perfil de usuario tiene para cada valor de
     * cada característica.
     */
    public BooleanItemProfileStatic(int idUser, Map<Feature, Map<Object, Double>> values) {
        this._idUser = idUser;
        this._values = values;
        this._weights = null;
    }

    @Override
    public double getFeatureValueValue(Feature f, Object featureValue) {
        return _values.get(f).get(featureValue.toString());
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
        return _values.containsKey(f) && _values.get(f).containsKey(value.toString());
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
