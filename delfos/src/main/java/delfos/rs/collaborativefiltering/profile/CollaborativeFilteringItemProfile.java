package delfos.rs.collaborativefiltering.profile;

import java.io.Serializable;

/**
 * Clase general de un perfil de producto.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0
 */
public abstract class CollaborativeFilteringItemProfile implements Serializable {
    
    private static final long serialVersionUID = 105L;

    /**
     * Id del producto al que se refiere este perfil.
     */
    protected final int idItem;

    /**
     * Devuelve el Id del producto al que se refiere el perfil.
     *
     * @return Id del producto al que se refiere el perfil.
     */
    public int getIdItem() {
        return idItem;
    }

    /**
     * Constructor por defecto del perfil de producto.
     *
     * @param idItem Producto al que se refiere este perfil.
     */
    public CollaborativeFilteringItemProfile(int idItem) {
        this.idItem = idItem;
    }
}
