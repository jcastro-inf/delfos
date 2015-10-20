package delfos.dataset.basic.rating;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Extiende la funcionalidad de la clase {@link Rating} para añadir una marca de
 * tiempo al mismo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 24-jul-2013
 */
public class RatingWithTimestamp extends Rating {

    private final long timestamp;

    public RatingWithTimestamp(int idUser, int idItem, Number rating, long timestamp) {
        super(idUser, idItem, rating);
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rating) {
            Rating r = (Rating) obj;
            return ((getIdUser() == r.getIdUser()) && (getIdItem() == r.getIdItem()));
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + this.getIdUser();
        hash = 47 * hash + this.getIdItem();
        hash = 59 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        return hash;
    }

    @Override
    public int compareTo(Rating o) {
        if (o.getIdUser() == getIdUser()) {
            if (o.getIdItem() == getIdItem()) {
                if (o instanceof RatingWithTimestamp) {
                    RatingWithTimestamp ratingWithTimestamp = (RatingWithTimestamp) o;
                    if (ratingWithTimestamp.timestamp != this.timestamp) {
                        if (ratingWithTimestamp.timestamp < this.timestamp) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else {
                        return 0;
                    }
                } else {
                    return 0;
                }
            } else {
                if (o.getIdItem() < getIdItem()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        } else {
            if (o.getIdUser() < getIdUser()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * Devuelve la marca de tiempo del ratingValue.
     *
     * @return Marca de tiempo del ratingValue, The time stamps are unix seconds
     * since 1/1/1970 UTC.
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Rating clone() throws CloneNotSupportedException {
        return new RatingWithTimestamp(getIdUser(), getIdItem(), getRatingValue(), timestamp);
    }

    @Override
    public String toString() {
        Calendar timestampCalendar = Calendar.getInstance();

        timestampCalendar.setTimeInMillis(timestamp);

        String ratingString = new DecimalFormat("#.###").format(getRatingValue());
        String timestampString = timestampCalendar.get(GregorianCalendar.YEAR) + "-" + timestampCalendar.get(GregorianCalendar.MONTH) + "-" + timestampCalendar.get(GregorianCalendar.DATE);

        return "(u=" + getIdUser() + " i=" + getIdItem() + " r=" + ratingString + " t=" + timestampString + ")";
    }

}
