package delfos.common.exceptions.dataset.entity;

/**
 * Excepción que se lanza al intentar buscar una entidad que no existe en la
 * colección {@link CollectionOfEntitiesWithFeatures}.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 4-Octubre-2013
 */
public class EntityNotFound extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final int id;
    private final Class<?> entityClass;

    /**
     * Crea la excepción a partir del identificador de la entidad que no se
     * encuentra.
     *
     * @param entityClass
     * @param id Identificador de la entidad no encontrada.
     */
    public EntityNotFound(Class<?> entityClass, int id) {
        super("Entitiy of class " + entityClass.getName() + " '" + id + "' not found");
        this.id = id;
        this.entityClass = entityClass;
    }

    public EntityNotFound(Class<?> entityClass, int id, Throwable cause) {
        super("Entitiy '" + id + "' not found", cause);
        this.id = id;
        this.entityClass = entityClass;
    }

    public EntityNotFound(Class<?> entityClass, int id, String msg) {
        super(msg);
        this.id = id;
        this.entityClass = entityClass;
    }

    public EntityNotFound(Class<?> entityClass, int id, Throwable cause, String msg) {
        super(msg, cause);
        this.id = id;
        this.entityClass = entityClass;
    }

    public int getIdEntity() {
        return id;
    }

    /**
     * Comprueba el tipo de entidad que generó la excepción. Si no es la
     * correcta, se lanza una {@link IllegalStateException}
     *
     * @param classToCompare Clase de la que debe ser la entidad para que no
     * lanze error.
     */
    public void isA(Class<?> classToCompare) {
        if (!entityClass.isAssignableFrom(classToCompare)) {
            throw new IllegalStateException("This error entity is a " + entityClass + " not a " + classToCompare);
        }
    }
}
