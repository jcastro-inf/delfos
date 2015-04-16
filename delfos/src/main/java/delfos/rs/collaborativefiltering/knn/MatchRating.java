package delfos.rs.collaborativefiltering.knn;

import delfos.dataset.basic.rating.Rating;

/**
 * Clase que encapsula los datos referentes a una coinicidencia de rating que se
 * puede usar posteriormente en la predicción de la valoración en sistemas de
 * recomendación colaborativos basados en vecinos cercanos. Permite almacenar
 * junto con el rating la ponderacion del mismo al usarlo en predicción y el
 * tipo de entidad a la que se recomienda.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date.
 * @version 1.1 19-Abril-2013
 */
public class MatchRating {

    private final RecommendationEntity entity;
    private final int idUser;
    private final int idItem;
    private final Number ratingValue;
    private final float weight;

    /**
     * Constructor del objeto.
     *
     * @param entity Entidad común a todos los match rating que se usen.
     * @param idUser id del usuario que realiza la valoración
     * @param idItem id del item sobre el que se realiza la valoración
     * @param rating valoración de preferencia
     * @param weight Peso de la valoración sobre el total (viene determinado por
     * la similitud de usuarios, de items, etc.)
     */
    public MatchRating(RecommendationEntity entity, int idUser, int idItem, Number rating, float weight) {
        this.entity = entity;
        this.idUser = idUser;
        this.idItem = idItem;
        this.ratingValue = rating;
        this.weight = weight;
    }
    
    /**
     * Constructor del objeto.
     *
     * @param entity Entidad común a todos los match rating que se usen.
     * @param idUser id del usuario que realiza la valoración
     * @param idItem id del item sobre el que se realiza la valoración
     * @param rating valoración de preferencia
     * @param weight Peso de la valoración sobre el total (viene determinado por
     * la similitud de usuarios, de items, etc.)
     */
    public MatchRating(RecommendationEntity entity, int idUser, int idItem, Rating rating, float weight) {
        this.entity = entity;
        this.idUser = idUser;
        this.idItem = idItem;
        this.ratingValue = rating.ratingValue;
        this.weight = weight;
    }

    /**
     * Devuelve el id del producto al que se refiere el rating.
     *
     * @return ID del producto.
     */
    public int getIdItem() {
        return idItem;
    }

    /**
     * Devuelve el id del usuario al que se refiere el rating.
     *
     * @return ID del usuario.
     */
    public int getIdUser() {
        return idUser;
    }

    /**
     * Devuelve la valoración.
     * @return  Valoración.
     */
    public Number getRating() {
        return ratingValue;
    }

    /**
     * Devuelve la ponderación con que se debe tener en cuenta esta valoración.
     * @return Ponderación de la valoración.
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Entidad común a los match rating que se usen.
     *
     * @return
     */
    public RecommendationEntity getEntity() {
        return entity;
    }
}
