/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.rs.collaborativefiltering.knn;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;

/**
 * Clase que encapsula los datos referentes a una coinicidencia de rating que se
 * puede usar posteriormente en la predicción de la valoración en sistemas de
 * recomendación colaborativos basados en vecinos cercanos. Permite almacenar
 * junto con el rating la ponderacion del mismo al usarlo en predicción y el
 * tipo de entidad a la que se recomienda.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date.
 * @version 1.1 19-Abril-2013
 */
public class MatchRating {

    private final RecommendationEntity entity;
    private final User user;
    private final Item item;
    private final Number ratingValue;
    private final double weight;

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
    @Deprecated
    public MatchRating(RecommendationEntity entity, int idUser, int idItem, Number rating, double weight) {
        this.entity = entity;
        this.user = new User(idUser);
        this.item = new Item(idItem);
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
    @Deprecated
    public MatchRating(RecommendationEntity entity, int idUser, int idItem, Rating rating, double weight) {
        this.entity = entity;
        this.user = new User(idUser);
        this.item = new Item(idItem);
        this.ratingValue = rating.getRatingValue();
        this.weight = weight;
    }

    public MatchRating(RecommendationEntity entity, User user, Item item, Number ratingValue, double weight) {
        this.entity = entity;
        this.user = user;
        this.item = item;
        this.ratingValue = ratingValue;
        this.weight = weight;
    }

    /**
     * Devuelve el id del producto al que se refiere el rating.
     *
     * @return ID del producto.
     */
    public int getIdItem() {
        return item.getId();
    }

    /**
     * Devuelve el id del usuario al que se refiere el rating.
     *
     * @return ID del usuario.
     */
    public int getIdUser() {
        return user.getId();
    }

    /**
     * Devuelve la valoración.
     *
     * @return Valoración.
     */
    public Number getRating() {
        return ratingValue;
    }

    /**
     * Devuelve la ponderación con que se debe tener en cuenta esta valoración.
     *
     * @return Ponderación de la valoración.
     */
    public double getWeight() {
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

    public Item getItem() {
        return item;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("(u: ").append(user)
                .append(", i: ").append(item)
                .append(") --> ").append(ratingValue)
                .append("(w: ").append(weight).append(")");

        return str.toString();
    }

}
