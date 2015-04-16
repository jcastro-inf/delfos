package delfos.dataset.basic.rating;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Clase para iterar de forma genérica sobre un dataset de valoraciones.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 07-Mar-2013 Implementada como clase, en lugar de como clase
 * interna de {@link RatingsDatasetAdapter}.
 * @param <RatingType>
 */
public class IteratorRatingsDataset<RatingType extends Rating> implements Iterator<RatingType> {

    private RatingType _next;
    private final LinkedList<Integer> _users;
    private final LinkedList<RatingType> _ratings;
    private final RatingsDataset<RatingType> _ratingsDataset;
    private final Object exMut = 0;

    /**
     * Crea el iterador para recorrer todos los ratings del dataset indicado.
     *
     * @param ratingsDataset
     */
    public IteratorRatingsDataset(RatingsDataset<RatingType> ratingsDataset) {
        _users = new LinkedList<Integer>(ratingsDataset.allUsers());
        _ratingsDataset = ratingsDataset;
        _ratings = new LinkedList<RatingType>();
        loadNextRating();
    }

    @Override
    public boolean hasNext() {
        synchronized (exMut) {
            return _next != null;
        }
    }

    @Override
    public RatingType next() {
        synchronized (exMut) {
            RatingType ret = _next;

            loadNextRating();

            return ret;
        }
    }

    @Override
    public void remove() {
        throw new IllegalStateException("Not allowed method.");
    }

    private void loadNextRating() {
        if (_ratings.isEmpty()) {
            //Lista vacía, cargar siguientes ratings.

            if (_users.isEmpty()) {
                ///No hay más usuarios, finalizar.
                _next = null;
            } else {
                //Hay mas usuarios, cargar sus ratings.
                int idUser = _users.remove(0);
                try {
                    for (Map.Entry<Integer, RatingType> entry : _ratingsDataset.getUserRatingsRated(idUser).entrySet()) {
                        _ratings.add(entry.getValue());
                    }
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }

                //Lista cargada, preparar el siguiente rating.
                _next = _ratings.remove(0);
            }
        } else {
            //La lista de ratings no está vacía, preparar siguiente rating
            _next = _ratings.remove(0);
        }
    }
}
