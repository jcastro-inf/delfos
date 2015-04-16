package delfos.rs.contentbased.vsm.booleanvsm.profile;

import java.io.Serializable;
import java.util.Set;
import delfos.dataset.basic.features.Feature;
import delfos.rs.UserProfile;

/**
 * Interfaz que establece los métodos de un perfil de usuario para sistemas de
 * recomendación basados en contenido.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 (18 Octubre 2011)
 * @vserion 2.0 (28 de Febrero de 2013) Separación de los métodos para las
 * ponderaciones mediante la clase {@link ContentBasedUserProfile_Weighted}
 */
public interface BooleanUserProfile extends UserProfile, Serializable {

    /**
     * Obtiene el valor de una característica determinada. El parámetro
     * <code>value</code> de la característica es ignorado si la característica
     * tiene el mismo peso independientemente de su valor
     *
     * @param f Característica para la que se desea conocer el peso
     * @param featureValue Valor de la característica para la que se desea
     * conocer el peso. Es ignorado si una característica tiene el mismo peso
     * independientemente de su valor
     * @return valor de la caracteristica en el perfil
     */
    public double getFeatureValueValue(Feature f, Object featureValue);

    /**
     * Obtiene el peso de una característica determinada. El parámetro
     * <code>value</code> de la característica es ignorado si una característica
     * tiene el mismo peso independientemente de su valor
     *
     * @param f Característica para la que se desea conocer el peso
     * @param featureValue Valor de la característica para la que se desea
     * conocer el peso. Es ignorado si una característica tiene el mismo peso
     * independientemente de su valor
     * @return peso de la característica en el perfil
     */
    public abstract double getFeatureValueWeight(Feature f, Object featureValue);

    /**
     * Devuelve los valores de características valorados por el usuario al que
     * pertenece este perfil
     *
     * @param f Característica para la que se desea conocer los valores
     * valorados
     * @return Conjunto de valores de la característica
     */
    public Set<Object> getValuedFeatureValues(Feature f);

    /**
     * Devuelve true si el perfil de usuario tiene valorado el valor
     * <code>value</code> de la característica
     * <code>f</code>.
     *
     * @param f Posible característica de un item.
     * @param value Valor de la característica f.
     * @return true si el perfil de usuario tiene valores para el par
     * (característica,valor)
     */
    public boolean contains(Feature f, Object value);

    /**
     * Devuelve las características que han sido valoradas por el usuario al que
     * pertenece este perfil
     *
     * @return Conjunto de características valoradas
     */
    public Iterable<Feature> getFeatures();

    /**
     * Método a implementar para liberar los recursos de este perfil de usuario.
     */
    public void cleanProfile();
}
