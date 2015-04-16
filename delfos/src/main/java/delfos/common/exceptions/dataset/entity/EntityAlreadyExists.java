package delfos.common.exceptions.dataset.entity;

/**
 * Excepción que se lanza al intentar añadir una entidad que ya existe (con
 * mismo identificador) a un {@link CollectionOfEntitiesWithFeatures}.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 4-Octubre-2013
 */
public class EntityAlreadyExists extends Error {

    private static final long serialVersionUID = 1L;
    private final int id;

    /**
     * Crea la excepción a partir del identificador de la entidad que se repite
     *
     * @param id Identificador de la entidad.
     */
    public EntityAlreadyExists(int id) {
        super("Entitiy '" + id + "' not found");
        this.id = id;
    }

    public EntityAlreadyExists(int id, Throwable cause) {
        super("Entitiy '" + id + "' not found", cause);
        this.id = id;
    }

    public int getIdEntity() {
        return id;
    }
}
