package delfos.rs.contentbased.vsm.booleanvsm.symeonidis2007;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.features.Feature;
import delfos.rs.contentbased.vsm.booleanvsm.profile.BooleanUserProfile;

/**
 * Perfil de usuario en el sistema {@link Symeonidis2007FeatureWeighted}.
 * Utiliza ponderacion de características pero no la almacena como tal, ya que
 * la combina con el valor del perfil mediante una multiplicación, por lo que no
 * es necesario almacenar un vector de ponderaciones.
 *
 * @author Jorge
 */
public class Symeonidis2007UserProfile implements BooleanUserProfile {

    private static final long serialVersionUID = 112L;

    /**
     * Id del usuario al que se refiere este perfil.
     */
    private final int _idUser;
    /**
     * Valor del perfil para cada valor de cada característica.
     */
    private Map<Feature, Map<Object, Double>> _values;

    /**
     * Constructor que genera un perfil de usuario con ponderación de
     * características a partir de los valores previamente calculados.
     *
     * @param idUser Id del usuario al que representa este perfil.
     * @param userProfileValues Valor que el perfil de usuario tiene para cada
     * una de los características posibles en el dataset de contenido.
     */
    public Symeonidis2007UserProfile(int idUser, Map<Feature, Map<Object, Double>> userProfileValues) {
        this._idUser = idUser;
        this._values = userProfileValues;
    }

    @Override
    public double getFeatureValueWeight(Feature f, Object featureValue) {
        throw new IllegalArgumentException("No se tiene ponderación de características.");
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
    public int getId() {
        return _idUser;
    }

    @Override
    public void cleanProfile() {
        for (Feature key : _values.keySet()) {
            _values.get(key).clear();
        }
        _values.clear();
        _values = new TreeMap<Feature, Map<Object, Double>>();
    }
}
