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
package delfos.rs.contentbased.vsm.multivalued.profile;

import delfos.common.aggregationoperators.AggregationOperator;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.Item;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Perfil de usuario multivaluado. Se utiliza en los sistema de recomendación
 * basados en contenido multivaluados.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class BasicMultivaluedUserProfile implements MultivaluedUserProfile {

    private static final long serialVersionUID = -3387516993124229948L;
    /**
     * Valor para cada valor de cada característica nominal.
     */
    protected Map<Feature, Map<Object, Double>> _nominalValues;
    /**
     * Valor para cada característica numérica.
     */
    protected Map<Feature, Double> _numericalValues;
    /**
     * Ponderación para cada característica.
     */
    protected Map<Feature, Double> _weights;
    /**
     * Id del usuario al que se refiere el perfil.
     */
    protected final long _idUser;

    /**
     * Construye un perfil de usuario vacío, al que posteriormente se deben
     * asignar items valorados, crear los pesos, aplicar iuf y normalizar.
     *
     * @param idUser Id del usuario al que se refiere el perfil.
     */
    public BasicMultivaluedUserProfile(long idUser) {
        this._idUser = idUser;
        _nominalValues = new TreeMap<>();
        _numericalValues = new TreeMap<>();
    }

    /**
     * Construye un perfil de usuario completo, asignando todos sus valores.
     *
     * @param idUser Id del usuario al que se refiere el perfil.
     * @param valuesNominal Valores para cada valor de cada característica
     * nominal.
     * @param valuesNumerical Valor para cada característica numérica.
     */
    public BasicMultivaluedUserProfile(long idUser, Map<Feature, Map<Object, Double>> valuesNominal, Map<Feature, Double> valuesNumerical) {
        this(idUser);
        this._nominalValues = valuesNominal;
        this._numericalValues = valuesNumerical;
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
                Map<Object, Double> get = _nominalValues.get(itemFeature);
                if (get.containsKey(value)) {
                    return get.get(value);
                }
            } else {
                throw new IllegalArgumentException("Undefined nominal item feature " + itemFeature);
            }
        }

        if (itemFeature.getType() == FeatureType.Numerical) {
            if (_numericalValues.containsKey(itemFeature)) {
                return _numericalValues.get(itemFeature);
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
    public void addItems(Set<Item> items, AggregationOperator condensationFormula) {
        TreeMap<Feature, Set<Number>> profileNumericalValues = new TreeMap<Feature, Set<Number>>();

        for (Item i : items) {
            for (Feature itemFeature : i.getFeatures()) {

                switch (itemFeature.getType()) {
                    case Nominal:
                        Object featureValue = i.getFeatureValue(itemFeature);
                        if (_nominalValues.containsKey(itemFeature)) {
                            Map<Object, Double> treeMap = _nominalValues.get(itemFeature);
                            if (treeMap.containsKey(featureValue)) {
                                treeMap.put(featureValue, treeMap.get(featureValue) + 1);
                            } else {
                                treeMap.put(featureValue, 1.0);
                            }
                        } else {
                            Map<Object, Double> treeMap = new TreeMap<Object, Double>();
                            treeMap.put(featureValue, 1.0);
                            _nominalValues.put(itemFeature, treeMap);
                        }
                        break;

                    case Numerical:
                        if (profileNumericalValues.containsKey(itemFeature)) {
                            profileNumericalValues.get(itemFeature).add((Number) i.getFeatureValue(itemFeature));
                        } else {
                            Set<Number> lista = new TreeSet<Number>();
                            lista.add((Number) i.getFeatureValue(itemFeature));
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
                double valor = _nominalValues.get(f).get(value);
                valor = valor / items.size();
                _nominalValues.get(f).put(value, valor);
            }
        }

        _numericalValues = new TreeMap<Feature, Double>();
        for (Feature f : profileNumericalValues.keySet()) {
            Set<Number> get = profileNumericalValues.get(f);
            _numericalValues.put(f, condensationFormula.aggregateValues(get));
        }
    }

    /**
     * Devuelve las características definidas para este perfil de usuario.
     *
     * @return Características.
     */
    @Override
    public Iterable<Feature> getFeatures() {
        Set<Feature> ret = new TreeSet<Feature>();
        for (Feature nominal : _nominalValues.keySet()) {
            ret.add(nominal);
        }
        for (Feature numerical : _numericalValues.keySet()) {
            ret.add(numerical);
        }
        return ret;
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
    public boolean contains(Feature itemFeature, Object featureValue) {
        boolean ret;
        switch (itemFeature.getType()) {
            case Nominal:
                if (_nominalValues.containsKey(itemFeature)) {
                    ret = _nominalValues.get(itemFeature).containsKey(featureValue);
                } else {
                    ret = false;
                }
                break;
            case Numerical:
                ret = _numericalValues.containsKey(itemFeature);
                break;
            default:
                throw new UnsupportedOperationException("The item feature type " + itemFeature.getType() + " isn't supported");
        }
        return ret;
    }

    /**
     * Devuelve el Id del usuario al que se refiere este perfil.
     *
     * @return Id del usuario.
     */
    @Override
    public final long getId() {
        return _idUser;
    }

    @Override
    public String toString() {
        return "profile " + _idUser;
    }

    @Override
    public double getFeatureValueWeight(Feature itemFeature) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
