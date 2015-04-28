package delfos.rs.contentbased.vsm.booleanvsm.profile;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.common.Global;

/**
 * Perfil de usuario utilizado por el sistema de recomendación basado en
 * contenido
 * <code>{@link TfIdfCBRS}</code>
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 (18 Octubre 2011)
 * @version 1.1 (21-Feb-2013) Convertido en objeto serializable. Eliminado el
 * atributo que almacenaba el dataset de contenido, ahora es necesario
 * especificarlo en el método {@link #getVectorProfile(delfos.Dataset.ContentDataset)
 * }.
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
    private Map<Feature, Map<Object, Float>> _profileValues;
    /**
     * Ponderación para cada característica. Como es un perfil booleano, las
     * características se componen de (característica,valor).
     */
    private Map<Feature, Map<Object, Float>> _profileWeights;
    /**
     * Conjunto de productos que el usuario ha valorado.
     */
    private Set<Integer> _valuedItems;
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
        _profileValues = new TreeMap<Feature, Map<Object, Float>>();
        _profileWeights = new TreeMap<Feature, Map<Object, Float>>();
        _valuedItems = new TreeSet<Integer>();
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
                    Map<Object, Float> treeMap = _profileValues.get(f);
                    if (treeMap.containsKey(value)) {
                        float featureValue = treeMap.get(value);
                        treeMap.put(value, featureValue + 1);
                    } else {
                        treeMap.put(value, 1.0f);
                    }
                } else {
                    Map<Object, Float> treeMap = new TreeMap<Object, Float>();
                    treeMap.put(value, 1.0f);
                    _profileValues.put(f, treeMap);
                }
            }
            _valuedItems.add(i.getId());
        }
        if (_valuedItems != null) {
            _valuedItems.add(i.getId());
        }
    }

//    /**
//     * Crea los pesos iniciales del perfil basándose en el numero de items
//     * valorados positivamente por el usuario al que pertenece el perfil
//     */
//    public void createOcurrencyBasedWeights() {
//        _weights = new TreeMap<Feature, Map<Object, Float>>();
//        for (Feature f : _profileValues.keySet()) {
//            _weights.put(f, new TreeMap<Object, Float>());
//
//            Map<Object, Float> get = _profileValues.get(f);
//
//            for (Object value : get.keySet()) {
//                _weights.get(f).put(value, get.get(value).floatValue());
//            }
//        }
//    }
//    /**
//     * Aplica las frecuencias inversas (Inverse User Frequence) a los pesos del
//     * perfil.
//     *
//     * @param allIuf Frecuencias inversas de cada (característica,valor)
//     */
//    public void applyInverseUserFrequence(Map<Feature, Map<Object, Float>> allIuf) {
//        for (Feature f : _weights.keySet()) {
//            Map<Object, Float> get = _weights.get(f);
//
//            for (Object value : get.keySet()) {
//                float weight = _weights.get(f).get(value);
//                weight = weight * allIuf.get(f).get(value);
//                _weights.get(f).put(value, weight);
//            }
//        }
//    }
    /**
     * Normaliza los valores del perfil dividiendo por el numero de peliculas
     * valoradas positivamente
     */
    public void normalizeValues() {
        for (Feature f : _profileValues.keySet()) {
            Map<Object, Float> get = _profileValues.get(f);
            for (Object value : get.keySet()) {
                float featureValue = get.get(value);
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
        float norma = 0;
        for (Feature f : _profileWeights.keySet()) {
            Map<Object, Float> get = _profileWeights.get(f);
            for (Object value : get.keySet()) {
                norma += _profileWeights.get(f).get(value);
            }
        }

        for (Feature f : _profileWeights.keySet()) {
            Map<Object, Float> get = _profileWeights.get(f);

            for (Object value : get.keySet()) {
                float weight = _profileWeights.get(f).get(value);
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
        Map<Object, Float> get = _profileValues.get(f);
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
    public int getId() {
        return _idUser;
    }

    @Override
    public void cleanProfile() {
        _valuedItems.clear();
        _valuedItems = new TreeSet<Integer>();

        for (Feature feature : getFeatures()) {
            for (Object featureValue : getValuedFeatureValues(feature)) {
                if (this.getFeatureValueValue(feature, featureValue) == 0) {
                    _profileValues.get(feature).remove(featureValue);
                    Global.showInfoMessage("DEL: " + feature + "-->" + featureValue);
                }
            }
        }
    }

    public void setWeight(Feature f, Object value, float weight) {
        if (!_profileWeights.containsKey(f)) {
            _profileWeights.put(f, new TreeMap<Object, Float>());
        }

        _profileWeights.get(f).put(value, weight);
    }
}
