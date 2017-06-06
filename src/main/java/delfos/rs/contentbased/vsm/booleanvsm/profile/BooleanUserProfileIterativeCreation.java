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
package delfos.rs.contentbased.vsm.booleanvsm.profile;

import delfos.common.Global;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.item.Item;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Perfil de usuario utilizado por el sistema de recomendación basado en
 * contenido.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class BooleanUserProfileIterativeCreation implements BooleanUserProfile, Serializable {

    private static final long serialVersionUID = 111l;
    /**
     * Id del usuario al que se refiere este perfil.
     */
    private final int _idUser;
    /**
     * Valores del perfil de usuario para cada característica. Como es un perfil
     * booleano, las características se componen de (característica,valor).
     */
    private final Map<Feature, Map<Object, Double>> _profileValues;
    /**
     * Ponderación para cada característica. Como es un perfil booleano, las
     * características se componen de (característica,valor).
     */
    private final Map<Feature, Map<Object, Double>> _profileWeights;
    /**
     * Conjunto de productos que el usuario ha valorado.
     */
    private Set<Long> _valuedItems;
    /**
     * Número de productos que el usuario ha valorado positivamente.
     */
    private int _numValoradasPositivamente = 0;

    /**
     * Construye un perfil de usuario vacío, al que posteriormente se deben
     * asignar items valorados, crear los pesos, aplicar iuf y normalizar.
     *
     * @param idUser Id del usuario al que se refiere este perfil.
     */
    public BooleanUserProfileIterativeCreation(int idUser) {
        this._idUser = idUser;
        _profileValues = new TreeMap<>();
        _profileWeights = new TreeMap<>();
        _valuedItems = new TreeSet<>();
    }

    @Override
    public double getFeatureValueWeight(Feature f, Object featureValue) {
        if (f == null || featureValue == null) {
            return 0;
        }
        if (_profileWeights.containsKey(f)) {
            if (_profileWeights.get(f).containsKey(featureValue)) {
                return _profileWeights.get(f).get(featureValue);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public double getFeatureValueValue(Feature f, Object featureValue) {
        if (f == null || featureValue == null) {
            return 0;
        }
        if (_profileValues.containsKey(f)) {
            if (_profileValues.get(f).containsKey(featureValue)) {
                return _profileValues.get(f).get(featureValue);
            }
        }
        return 0;
    }

    /**
     * Añade un item al perfil para que sea tomado en cuenta como valorado
     * positivamente (relevante) en el perfil de usuario.
     *
     * @param i Contenido del producto que se desea agregar
     * @param relevant Indica si el producto es relevante para el usuario o no.
     */
    public void addItem(Item i, boolean relevant) {
        if (_valuedItems.contains(i.getId())) {
            throw new IllegalArgumentException("The profile already contains the item " + i);
        } else {
        }

        if (relevant && _valuedItems != null && !_valuedItems.contains(i.getId())) {
            _numValoradasPositivamente++;
            for (Feature f : i.getFeatures()) {
                Object value = i.getFeatureValue(f);
                if (_profileValues.containsKey(f)) {
                    Map<Object, Double> treeMap = _profileValues.get(f);
                    if (treeMap.containsKey(value)) {
                        double featureValue = treeMap.get(value);
                        treeMap.put(value, featureValue + 1);
                    } else {
                        treeMap.put(value, 1.0);
                    }
                } else {
                    Map<Object, Double> treeMap = new TreeMap<Object, Double>();
                    treeMap.put(value, 1.0);
                    _profileValues.put(f, treeMap);
                }
            }
            _valuedItems.add(i.getId());
        }
        if (_valuedItems != null) {
            _valuedItems.add(i.getId());
        }
    }

    /**
     * Normaliza los valores del perfil dividiendo por el numero de peliculas
     * valoradas positivamente
     */
    public void normalizeValues() {
        for (Feature f : _profileValues.keySet()) {
            Map<Object, Double> get = _profileValues.get(f);
            for (Object value : get.keySet()) {
                double featureValue = get.get(value);
                featureValue = featureValue / _numValoradasPositivamente;
                get.put(value, featureValue);
            }
        }
    }

    /**
     * Normaliza los pesos del perfil. Un vector de pesos normalizado es aquel
     * que cumple la propiedad Sum(pesos) = 1
     */
    public void normalizeWeights() {
        double norma = 0;
        for (Feature f : _profileWeights.keySet()) {
            Map<Object, Double> get = _profileWeights.get(f);
            for (Object value : get.keySet()) {
                norma += _profileWeights.get(f).get(value);
            }
        }

        for (Feature f : _profileWeights.keySet()) {
            Map<Object, Double> get = _profileWeights.get(f);

            for (Object value : get.keySet()) {
                double weight = _profileWeights.get(f).get(value);
                weight = weight / norma;
                _profileWeights.get(f).put(value, weight);
            }
        }
    }

    @Override
    public Set<Feature> getFeatures() {
        return _profileValues.keySet();
    }

    @Override
    public Set<Object> getValuedFeatureValues(Feature f) {
        Map<Object, Double> get = _profileValues.get(f);
        if (get == null) {
            return new TreeSet<Object>();
        } else {
            return get.keySet();
        }
    }

    @Override
    public boolean contains(Feature f, Object value) {
        if (_profileValues.containsKey(f) && _profileValues.get(f).containsKey(value)) {
            return _profileValues.get(f).containsKey(value);
        } else {
            return false;
        }
    }

    @Override
    public long getId() {
        return _idUser;
    }

    @Override
    public void cleanProfile() {
        _valuedItems.clear();
        _valuedItems = new TreeSet<>();

        for (Feature feature : getFeatures()) {
            for (Object featureValue : getValuedFeatureValues(feature)) {
                if (this.getFeatureValueValue(feature, featureValue) == 0) {
                    _profileValues.get(feature).remove(featureValue);
                    Global.showInfoMessage("DEL: " + feature + "-->" + featureValue);
                }
            }
        }
    }

    public void setWeight(Feature f, Object value, double weight) {
        if (!_profileWeights.containsKey(f)) {
            _profileWeights.put(f, new TreeMap<>());
        }

        _profileWeights.get(f).put(value, weight);
    }
}
