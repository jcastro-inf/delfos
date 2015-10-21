package delfos.dataset.basic.rating;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.IntegerDomain;
import delfos.dataset.basic.user.User;
import java.io.Serializable;
import java.text.DecimalFormat;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Clase que encapsula el almacenamiento en memoria de una valoración que un
 * usuario ha hecho sobre un producto.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date.
 * @version 1.1
 */
public class Rating implements Comparable<Rating>, Serializable, Cloneable {

    private static final long serialVersionUID = 4352436234L;
    public static IntegerDomain DEFAULT_INTEGER_DOMAIN = new IntegerDomain(1, 5);
    public static DecimalDomain DEFAULT_DECIMAL_DOMAIN = new DecimalDomain(1, 5);

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
    public Rating(int idUser, int idItem, Number rating) {
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

        } else {
            if (o.getIdUser() < getIdUser()) {
                return 1;
            } else {
                return -1;
            }
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

    @Override
    public Rating clone() throws CloneNotSupportedException {
        return new Rating(getIdUser(), getIdItem(), getRatingValue());
    }

    /**
     * @return the idUser
     */
    public int getIdUser() {
        return user.getId();
    }

    /**
     * @return the idItem
     */
    public int getIdItem() {
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
}
