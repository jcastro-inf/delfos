package delfos.rs.collaborativefiltering.knn;

/**
 * Entidades de recomendación. Una entidad de recomendación indica el tipo de
 * objeto al que se refiere cierta información, si es a un usuario o a un
 * producto.
 *
* @author Jorge Castro Gallardo
 * 
 * @version 1.0 Unknown date.
 * @version 1.1 25-Abril-2013.
 */
public enum RecommendationEntity {

    /**
     * Entidad de recomendación de usuario.
     */
    USER("User"),
    /**
     * Entidad de recomendación producto.
     */
    ITEM("Item");

    private RecommendationEntity(String name) {
        this.name = name;
    }
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
