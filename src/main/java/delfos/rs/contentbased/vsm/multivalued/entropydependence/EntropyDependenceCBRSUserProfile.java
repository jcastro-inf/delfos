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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureType;
import delfos.rs.contentbased.vsm.multivalued.profile.MultivaluedUserProfile;
import delfos.common.aggregationoperators.AggregationOperator;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 14-oct-2013
 */
public class EntropyDependenceCBRSUserProfile implements MultivaluedUserProfile {

    private static final long serialVersionUID = 1L;
    private final long _idUser;
    private final Map<Feature, Map<Object, Number>> _nominalValues;
    private final Map<Feature, Number> _numericalValues;
    private final Map<Feature, Number> _weights;

    private EntropyDependenceCBRSUserProfile() {
        _idUser = Integer.MIN_VALUE;
        _nominalValues = null;
        _numericalValues = null;
        _weights = null;
    }

    public EntropyDependenceCBRSUserProfile(long idUser,
            Map<Feature, Map<Object, Number>> nominalFeatures,
            Map<Feature, Number> numericalFeatures,
            Map<Feature, Number> weights) {
        this._idUser = idUser;

        this._nominalValues = new TreeMap<Feature, Map<Object, Number>>();
        //Copio la "matriz"
        for (Map.Entry<Feature, Map<Object, Number>> entry : nominalFeatures.entrySet()) {
            Feature feature = entry.getKey();
            Map<Object, Number> featureValues = entry.getValue();

            this._nominalValues.put(feature, new TreeMap<Object, Number>());
            for (Map.Entry<Object, Number> entry2 : featureValues.entrySet()) {
                Object featureValue = entry2.getKey();
                Number featureValueValue = entry2.getValue();
                this._nominalValues.get(feature).put(featureValue, featureValueValue);
            }
        }

        this._numericalValues = new TreeMap<Feature, Number>(numericalFeatures);
        this._weights = new TreeMap<Feature, Number>(weights);
    }

    @Override
    public long getId() {
        return _idUser;
    }

    @Override
    public Iterable<Feature> getFeatures() {
        return new ArrayList<Feature>(_weights.keySet());
    }

    /**
     * Devuelve el valor del perfil para el valor concreto de la característica.
     *
     * @param itemFeature Característica para la que se busca el valor.
     * @param value Valor de la característica para el que se busca el balor.
     * @return Valor del perfil para el valor y la característica indicados.
     *
     * @throws IllegalArgumentException Si no encuentra la característica o el
     * valor de la misma.
     */
    @Override
    public double getFeatureValueValue(Feature itemFeature, Object value) {
        if (itemFeature.getType() == FeatureType.Nominal) {
            if (_nominalValues.containsKey(itemFeature)) {
                Map<Object, Number> get = _nominalValues.get(itemFeature);
                if (get.containsKey(value)) {
                    return get.get(value).doubleValue();
                }
            } else {
                throw new IllegalArgumentException("Undefined nominal item feature " + itemFeature);
            }
        }

        if (itemFeature.getType() == FeatureType.Numerical) {
            if (_numericalValues.containsKey(itemFeature)) {
                return _numericalValues.get(itemFeature).doubleValue();
            } else {
                throw new IllegalArgumentException("Undefined numerical item feature " + itemFeature);
            }
        }
        throw new UnsupportedOperationException("Unknow item feature type " + itemFeature.getType());
    }

    /**
     * Añade un item al perfil para que sea tomado en cuenta como valorado
     * positivamente (relevante) en el perfil de usuario.
     *
     * @param items contenido del item que se desea agregar
     * @param condensationFormula formula para agregar los características
     * numéricos de los items relevantes en el perfil
     */
    private void addItems(Set<Item> items, AggregationOperator condensationFormula) {
        TreeMap<Feature, Set<Number>> profileNumericalValues = new TreeMap<Feature, Set<Number>>();

        for (Item i : items) {
            for (Feature itemFeature : i.getFeatures()) {

                switch (itemFeature.getType()) {
                    case Nominal:
                        Object featureValue = i.getFeatureValue(itemFeature);
                        if (_nominalValues.containsKey(itemFeature)) {
                            Map<Object, Number> treeMap = _nominalValues.get(itemFeature);
                            if (treeMap.containsKey(featureValue)) {
                                treeMap.put(featureValue, treeMap.get(featureValue).doubleValue() + 1);
                            } else {
                                treeMap.put(featureValue, 1.0f);
                            }
                        } else {
                            Map<Object, Number> treeMap = new TreeMap<Object, Number>();
                            treeMap.put(featureValue, 1.0f);
                            _nominalValues.put(itemFeature, treeMap);
                        }
                        break;

                    case Numerical:
                        if (profileNumericalValues.containsKey(itemFeature)) {
                            profileNumericalValues.get(itemFeature).add((Double) i.getFeatureValue(itemFeature));
                        } else {
                            Set<Number> lista = new TreeSet<Number>();
                            lista.add((Double) i.getFeatureValue(itemFeature));
                            profileNumericalValues.put(itemFeature, lista);
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("The item feature type " + itemFeature.getType() + " isn't supported");
                }
            }
        }

        for (Feature f : _nominalValues.keySet()) {
            for (Object value : _nominalValues.get(f).keySet()) {
                double valor = _nominalValues.get(f).get(value).doubleValue();
                valor = valor / items.size();
                _nominalValues.get(f).put(value, valor);
            }
        }

        for (Feature f : profileNumericalValues.keySet()) {
            Set<Number> get = profileNumericalValues.get(f);
            _numericalValues.put(f, condensationFormula.aggregateValues(get));
        }
    }

    /**
     * Devuelve los valores de la característica especificada definidos en este
     * perfil de usuario.
     *
     * @param itemFeature Característica para los que se busca sus valores.
     * @return Conjunto de valores de la característica. Si no contiene la
     * característica, devuelve null.
     */
    @Override
    public Set<Object> getValuedFeatureValues(Feature itemFeature) {
        if (itemFeature.getType() == FeatureType.Nominal) {
            return _nominalValues.get(itemFeature).keySet();
        }
        if (itemFeature.getType() == FeatureType.Numerical) {
            Set<Object> ret = new TreeSet<Object>();
            ret.add(_numericalValues.get(itemFeature));
            return ret;
        }
        throw new UnsupportedOperationException("The item feature type " + itemFeature.getType() + " isn't supported");
    }

    @Override
    public boolean contains(Feature feature, Object featureValue) {
        boolean ret;
        switch (feature.getType()) {
            case Nominal:
                if (_nominalValues.containsKey(feature)) {
                    ret = _nominalValues.get(feature).containsKey(featureValue);
                } else {
                    ret = false;
                }
                break;
            case Numerical:
                ret = _numericalValues.containsKey(feature);
                break;
            default:
                throw new IllegalArgumentException("The specified feature '" + feature + "'type '" + feature.getType() + "' is not supported.");
        }
        return ret;
    }

    @Override
    public String toString() {
        return "profile " + _idUser;
    }

    @Override
    public double getFeatureValueWeight(Feature feature) {
        if (_weights.containsKey(feature)) {
            return _weights.get(feature).doubleValue();
        } else {
            throw new IllegalArgumentException("The specified feature " + feature + " has no weight.");
        }
    }

    boolean contains(Feature feature) {
        boolean ret;
        switch (feature.getType()) {
            case Nominal:
                ret = _nominalValues.containsKey(feature);
                break;
            case Numerical:
                ret = _numericalValues.containsKey(feature);
                break;
            default:
                throw new IllegalArgumentException("The specified feature '" + feature + "'type '" + feature.getType() + "' is not supported.");
        }
        return ret;
    }
}
