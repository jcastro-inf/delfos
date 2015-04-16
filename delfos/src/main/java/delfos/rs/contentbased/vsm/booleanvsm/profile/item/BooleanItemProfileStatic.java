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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.1 9-Octubre-2013
 */
public class BooleanItemProfileStatic implements BooleanItemProfile, Serializable {

    private static final long serialVersionUID = 112L;
    /**
     * Id del usuario al que se refiere este perfil.
     */
    private final int _idUser;
    /**
     * Valor del perfil para cada valor de cada característica.
     */
    private Map<Feature, Map<Object, Float>> _values;
    /**
     * Ponderación de cada valor de cada característica.
     */
    private Map<Feature, Map<Object, Float>> _weights;

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
    public BooleanItemProfileStatic(int idUser, Map<Feature, Map<Object, Float>> values, Map<Feature, Map<Object, Float>> weights) {
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
    public BooleanItemProfileStatic(int idUser, Map<Feature, Map<Object, Float>> values) {
        this._idUser = idUser;
        this._values = values;
        this._weights = null;
    }

    @Override
    public float getFeatureValueValue(Feature f, Object featureValue) {
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
    public int getId() {
        return _idUser;
    }

    @Override
    public void cleanProfile() {
        for (Feature key : _values.keySet()) {
            _values.get(key).clear();
        }
        _values.clear();
        _values = new TreeMap<Feature, Map<Object, Float>>();

        for (Feature key : _weights.keySet()) {
            _weights.get(key).clear();
        }
        _weights.clear();
        _weights = new TreeMap<Feature, Map<Object, Float>>();
    }
}
