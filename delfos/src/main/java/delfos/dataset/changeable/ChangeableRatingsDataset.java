package delfos.dataset.changeable;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 * Interfaz que define los métodos adicionales para convertir un dataset de
 * valoraciones en modificable.
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public interface ChangeableRatingsDataset<RatingType extends Rating> extends RatingsDataset<RatingType> {

    /**
     * Añade una valoración dada al dataset de valoraciones. En las siguientes
     * llamadas a los métodos del dataset, el rating añadido será devuelto como
     * si hubiera estado en el dataset en el momento de su carga en memoria.
     *
     * @param idUser Usuario que da la valoración.
     * @param idItem Producto valorado.
     * @param rating Valor de la valoración.
     */
    public void addRating(int idUser, int idItem, RatingType rating);

    /**
     * Elimina una valoración dada del dataset de valoraciones. En las
     * siguientes llamadas a los métodos del dataset, el rating eliminado no
     * será tenido en cuenta.
     *
     * @param idUser Usuario que da la valoración.
     * @param idItem Producto valorado.
     *
     * @throws IllegalArgumentException Si el rating a eliminar no existía en el
     * dataset.
     */
    public void removeRating(int idUser, int idItem);

    /**
     * Ordena que los datos sean guardados en el método persistente
     * correspondiente del dataset.
     */
    public void commitChangesInPersistence();
}
