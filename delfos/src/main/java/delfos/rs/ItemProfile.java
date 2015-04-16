package delfos.rs;

import java.io.Serializable;

/**
 * Interfaz que define los métodos comunes a todos los periles de productos que
 * los sistemas de recomendación utilicen
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 9-Octubre-2013
 */
public interface ItemProfile extends Serializable {

    /**
     * Obtienen el identificador del producto al que pertenece este perfil.
     *
     * @return Identificador del producto al que pertenece este perfil.
     */
    public int getId();
}
