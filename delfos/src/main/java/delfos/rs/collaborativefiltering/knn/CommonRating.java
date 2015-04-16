package delfos.rs.collaborativefiltering.knn;

/**
 * Entidad que encapsula un rating en común. rating1 y rating2 son valoraciones
 * que tienen en común una entidad <b>commonEntity</b> con id <b> idCommon</b>.
 *
 *
 * <p>
 * <p>
 * Se utiliza, por ejemplo, para almacenar que el usuario u54 y el usuario u98
 * valoraron el producto i46 con una valoración de 3 y 5 respectivamente. En
 * este caso, {@link CommonRating#getCommonEntity() } indicara que es un
 * producto y {@link CommonRating#getRatingEntity() } indicará que es un
 * usuario. El método {@link CommonRating#getIdR1() } devuelve 54 y el método {@link CommonRating#getIdR3()
 * } devuelve 98.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date.
 * @version 1.1 20-Mar-2013
 * @version 1.2 19-Abril-2013 Completada la documentación de la clase.
 */
public class CommonRating {

    /**
     * Tipo de la entidad común, es decir, tipo al que todos los ratings se
     * refieren.
     */
    private final RecommendationEntity commonEntity;
    private final RecommendationEntity ratingEntity;
    private final int idCommon;
    private final float rating1;
    private final float rating2;
    private final int idR1;
    private final int idR2;
    private Float weight = null;

    /**
     * Crea una entidad de rating común, que almacena dos ratings dados sobre
     * una misma entidad.
     *
     * @param commonEntity Tipo de la entidad a la que se refieren las dos
     * valoraciones.
     * @param idCommon Id de la entidad en común.
     * @param ratingEntity Tipo de la entidad que es distinta.
     * @param idR1 Id de la entidad distinta 1.
     * @param idR2 Id de la entidad distinta 2.
     * @param rating1 Valoración 1.
     * @param rating2 Valoración 2.
     * @param weight Ponderación de la valoración.
     */
    public CommonRating(RecommendationEntity commonEntity, int idCommon, RecommendationEntity ratingEntity, int idR1, int idR2, float rating1, float rating2, float weight) {
        this.commonEntity = commonEntity;
        this.ratingEntity = ratingEntity;
        this.idCommon = idCommon;
        this.rating1 = rating1;
        this.rating2 = rating2;
        this.idR1 = idR1;
        this.idR2 = idR2;
        this.weight = weight;
    }

    /**
     * Crea una entidad de rating común, que almacena dos ratings dados sobre
     * una misma entidad.
     *
     * @param commonEntity Tipo de la entidad a la que se refieren las dos
     * valoraciones.
     * @param idCommon Id de la entidad en común.
     * @param ratingEntity Tipo de la entidad que es distinta.
     * @param idR1 Id de la entidad distinta 1.
     * @param idR2 Id de la entidad distinta 2.
     * @param rating1 Valoración 1.
     * @param rating2 Valoración 2.
     */
    public CommonRating(RecommendationEntity commonEntity, int idCommon, RecommendationEntity ratingEntity, int idR1, int idR2, float rating1, float rating2) {
        this.commonEntity = commonEntity;
        this.ratingEntity = ratingEntity;
        this.idCommon = idCommon;
        this.rating1 = rating1;
        this.rating2 = rating2;
        this.idR1 = idR1;
        this.idR2 = idR2;
    }

    /**
     * Devuelve el tipo de la entidad común, es decir, tipo al que todos los
     * ratings se refieren.
     *
     * @return Tipo de la entidad común, es decir, tipo al que todos los ratings
     * se refieren.
     */
    public RecommendationEntity getCommonEntity() {
        return commonEntity;
    }

    /**
     * Devuelve el ID de la entidad en común. La entidad de recomendación será
     * un usuario o un producto, dependiendo del valor del método
     * {@link CommonRating#commonEntity}.
     *
     * @return ID de la entidad en común.
     */
    public int getIdCommon() {
        return idCommon;
    }

    /**
     * Devuelve el ID de la entidad de valoración 1, que será un usuario o un
     * producto dependiendo del valor del método
     * {@link CommonRating#ratingEntity}.
     *
     * @return ID de la entidad de valoración 1.
     */
    public int getIdR1() {
        return idR1;
    }

    /**
     * Devuelve el ID de la entidad de valoración 2, que será un usuario o un
     * producto dependiendo del valor del método
     * {@link CommonRating#ratingEntity}.
     *
     * @return ID de la entidad de valoración 2.
     */
    public int getIdR2() {
        return idR2;
    }

    /**
     * Devuelve la valoración del objeto el común y la entidad de valoración 1.
     *
     * @return Valoración de la entidad de valoración 1.
     */
    public float getRating1() {
        return rating1;
    }

    /**
     * Devuelve la valoración del objeto el común y la entidad de valoración 2.
     *
     * @return Valoración de la entidad de valoración 2.
     */
    public float getRating2() {
        return rating2;
    }

    /**
     * Devuelve el tipo de la entidad de valoración, es decir, las entidades
     * diferentes.
     *
     * @return Tipo de la entidad de valoración.
     */
    public RecommendationEntity getRatingEntity() {
        return ratingEntity;
    }

    /**
     * Devuelve la ponderación del rating en común.
     *
     * @return Ponderación del rating en común.
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Devuelve true si el rating en común tiene una ponderación definida.
     *
     * @return true si el rating en común tiene una ponderación definida.
     */
    public boolean isWeighted() {
        return weight != null;
    }

    /**
     * Establece la ponderación de esta valoración en común.
     *
     * @param weight Ponderación de la valoración en común.
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }
}
