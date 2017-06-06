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
package delfos.dataset.basic.rating;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.IntegerDomain;
import delfos.dataset.basic.user.User;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Comparator;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Clase que encapsula el almacenamiento en memoria de una valoración que un usuario ha hecho sobre un producto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date.
 * @version 1.1
 */
public class Rating implements Comparable<Rating>, Serializable, Cloneable {

    private static final long serialVersionUID = 4352436234L;
    public static IntegerDomain DEFAULT_INTEGER_DOMAIN = new IntegerDomain(1, 5);
    public static DecimalDomain DEFAULT_DECIMAL_DOMAIN = new DecimalDomain(1, 5);

    public static final Comparator<? extends Rating> SORT_BY_ID_ITEM = (r1, r2) -> Item.BY_ID.compare(r1.item, r2.item);

    public static final Comparator<Rating> SORT_BY_RATING_DESC = (r1, r2) -> -Double.compare(r1.getRatingValue().doubleValue(), r2.getRatingValue().doubleValue());

    private final User user;

    private final Item item;

    /**
     * Valor de valoración que el usuario da sobre el producto
     */
    private final Number ratingValue;

    /**
     * Crea una valoracion.
     *
     * @param idUser
     * @param idItem
     * @param rating Valor concreto de la valoración.
     */
    @Deprecated
    public Rating(long idUser, long idItem, Number rating) {
        this.user = new User(idUser);
        this.item = new Item(idItem);
        this.ratingValue = rating;
    }

    public Rating(User user, Item item, Number ratingValue) {
        this.user = user;
        this.item = item;
        this.ratingValue = ratingValue;
    }

    @Override
    public int compareTo(Rating o) {
        if (o.getIdUser() == getIdUser()) {
            if (o.getIdItem() == getIdItem()) {
                if (o.getRatingValue().doubleValue() == o.getRatingValue().doubleValue()) {
                    return 0;
                } else if (o.getRatingValue().doubleValue() < getRatingValue().doubleValue()) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (o.getIdItem() < getIdItem()) {
                return 1;
            } else {
                return -1;
            }

        } else if (o.getIdUser() < getIdUser()) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rating) {
            Rating r = (Rating) obj;
            return ((getIdUser() == r.getIdUser()) && (getIdItem() == r.getIdItem()) && (getRatingValue().doubleValue() == r.getRatingValue().doubleValue()));
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {

        HashCodeBuilder hashBuilder = new HashCodeBuilder(37, 11);
        hashBuilder.append(getIdUser());
        hashBuilder.append(getIdItem());
        hashBuilder.append(getRatingValue().doubleValue());

        int hash = hashBuilder.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        String ratingString = new DecimalFormat("#.###").format(getRatingValue());
        return "(u=" + getIdUser() + " i=" + getIdItem() + " r=" + ratingString + ")";
    }

    /**
     * @return the idUser
     */
    public long getIdUser() {
        return user.getId();
    }

    /**
     * @return the idItem
     */
    public long getIdItem() {
        return item.getId();
    }

    public User getUser() {
        return user;
    }

    public Item getItem() {
        return item;
    }

    /**
     * @return the ratingValue
     */
    public Number getRatingValue() {
        return ratingValue;
    }

    public Rating copyWithUser(User user) {
        return new Rating(user, item, ratingValue);
    }

    public Rating copyWithItem(Item item) {
        return new Rating(user, item, ratingValue);
    }

    public Rating copyWithRatingValue(Number ratingValue) {
        return new Rating(user, item, ratingValue);
    }

    @Override
    public Rating clone() throws CloneNotSupportedException {
        return (Rating) super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
