package delfos.rs.contentbased.vsm.multivalued;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import delfos.rs.contentbased.vsm.multivalued.profile.MultivaluedUserProfile;

/**
 * Modelo de recomendación de los sistemas de recomendación que utilizan
 * modelado multivaluado de los productos y usuarios.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 28-May-2013
 */
public class MultivaluedUserProfilesModel implements Serializable {

    private static final long serialVersionUID = 113L;
    /**
     * Almacena los perfiles de usuario.
     */
    private final Map<Integer, MultivaluedUserProfile> userProfiles;

    public MultivaluedUserProfilesModel(Map<Integer, MultivaluedUserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }

    /**
     * Devuelve todos los perfiles de usuario.
     *
     * @return Perfiles de usuario.
     */
    public Map<Integer, MultivaluedUserProfile> getUserProfiles() {
        return Collections.unmodifiableMap(userProfiles);
    }
}
