package delfos.dataset.loaders.epinions;

import delfos.dataset.basic.rating.Rating;

/**
 * Rating del dataset de epinions, con toda la información que el dataset
 * contiene.
 * <p>
 * <p>
 * Original documentation:http://www.trustlet.org/wiki/Extended_Epinions_dataset
 *
* @author Jorge Castro Gallardo
 *
 * @version 12-Diciembre-2014
 */
public class EPinionsRating extends Rating {

    /**
     * Valor status del rating del dataset epinions. Si es true, el usuario ha
     * elegido no mostrar su nombre acompañando la valoración. Si es false, al
     * usuario no le importa que sea mostrado su nombre.
     */
    private final Boolean status_HideRating;

    /**
     * Fecha de primera creación de la valoración.
     */
    private final long timestamp_creation;

    /**
     * Fecha de la última modificación de la valoración. Si no se ha modificado
     * la valoración, coincide con el valor de creación.
     */
    private final long timestamp_lastModification;

    /**
     * Vertical id de la valoración.
     */
    private long idVertical;

    public EPinionsRating(Integer idUser, Integer idRated, Number rating) {
        super(idUser, idRated, rating);

        timestamp_creation = -1;
        timestamp_lastModification = -1;
        status_HideRating = false;
    }

    EPinionsRating(Integer MEMBER_ID, Integer OBJECT_ID, Number RATING, boolean STATUS, long CREATION, long LAST_MODIFIED, long VERTICAL_ID) {
        super(MEMBER_ID, OBJECT_ID, RATING);

        status_HideRating = STATUS;
        timestamp_creation = CREATION;
        timestamp_lastModification = LAST_MODIFIED;
        idVertical = VERTICAL_ID;
    }

    @Override
    public Rating clone() throws CloneNotSupportedException {
        return new EPinionsRating(idUser, idItem, ratingValue, status_HideRating, timestamp_creation, timestamp_lastModification, idVertical);
    }

}
