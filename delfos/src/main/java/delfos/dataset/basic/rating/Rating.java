package delfos.dataset.basic.rating;

import java.io.Serializable;
import java.text.DecimalFormat;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.IntegerDomain;

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

    /**
     * Identificador del usuario que da la valoración
     */
    public final int idUser;

    /**
     * Identificador del producto sobre el que da la valoración
     */
    public final int idItem;

    /**
     * Valor de valoración que el usuario da sobre el producto
     */
    public final Number ratingValue;

    /**
     * Crea una valoracion.
     *
     * @param idUser
     * @param idItem
     * @param rating Valor concreto de la valoración.
     */
    public Rating(int idUser, int idItem, Number rating) {
        this.idUser = idUser;
        this.idItem = idItem;
        this.ratingValue = rating;
    }

    @Override
    public int compareTo(Rating o) {
        if (o.idUser == idUser) {
            if (o.idItem == idItem) {
                if (o.ratingValue.doubleValue() == o.ratingValue.doubleValue()) {
                    return 0;
                } else if (o.ratingValue.doubleValue() < ratingValue.doubleValue()) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (o.idItem < idItem) {
                return 1;
            } else {
                return -1;
            }

        } else {
            if (o.idUser < idUser) {
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
            return ((idUser == r.idUser) && (idItem == r.idItem) && (ratingValue.doubleValue() == r.ratingValue.doubleValue()));
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {

        HashCodeBuilder hashBuilder = new HashCodeBuilder(37, 11);
        hashBuilder.append(idUser);
        hashBuilder.append(idItem);
        hashBuilder.append(ratingValue.doubleValue());

        int hash = hashBuilder.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        String ratingString = new DecimalFormat("#.###").format(ratingValue);
        return "(u=" + idUser + " i=" + idItem + " r=" + ratingString + ")";
    }

    @Override
    public Rating clone() throws CloneNotSupportedException {
        return new Rating(idUser, idItem, ratingValue);
    }
}
