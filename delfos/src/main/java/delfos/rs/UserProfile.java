package delfos.rs;

import java.io.Serializable;

/**
 * Interfaz que define los métodos comunes a todos los periles de usuario que
 * los sistemas de recomendación utilicen
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 9-Octubre-2013
 */
public interface UserProfile extends Serializable {

    /**
     * Obtienen el id de usuario al que pertenece el perfil
     *
     * @return Id del usuario al que pertenece el perfil
     */
    public int getId();
}
