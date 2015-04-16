package delfos.rs.contentbased.vsm.booleanvsm;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import delfos.rs.contentbased.vsm.booleanvsm.profile.BooleanUserProfile;

/**
 * Modelo de recomendación de los sistemas de recomendación que utilizan
 * modelado booleano de los productos y perfiles de usuarios calculados a partir
 * de la suma.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 28-May-2013
 */
public class BooleanContentBasedModel implements Serializable {

    private static final long serialVersionUID = 109L;
    /**
     * Almacena los perfiles de usuario.
     */
    private final Map<Integer, BooleanUserProfile> userProfiles;

    public BooleanContentBasedModel(Map<Integer, BooleanUserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }

    /**
     * Devuelve todos los perfiles de usuario.
     *
     * @return Perfiles de usuario.
     */
    public Map<Integer, BooleanUserProfile> getUserProfiles() {
        return Collections.unmodifiableMap(userProfiles);
    }
}
