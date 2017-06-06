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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Clase para iterar de forma genérica sobre un dataset de valoraciones.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 07-Mar-2013 Implementada como clase, en lugar de como clase interna de {@link RatingsDatasetAdapter}.
 * @param <RatingType>
 */
public class IteratorRatingsDataset<RatingType extends Rating> implements Iterator<RatingType> {

    private RatingType _next;
    private final List<Long> _users;
    private final List<RatingType> _ratings;
    private final RatingsDataset<RatingType> _ratingsDataset;
    private final Object exMut;

    /**
     * Crea el iterador para recorrer todos los ratings del dataset indicado.
     *
     * @param ratingsDataset
     */
    public IteratorRatingsDataset(RatingsDataset<RatingType> ratingsDataset) {
        _users = new LinkedList<>(ratingsDataset.allUsers());
        _ratingsDataset = ratingsDataset;
        _ratings = new LinkedList<>();
        exMut = ratingsDataset;
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
                long idUser = _users.remove(0);

                Map<Long, RatingType> userRatingsRated = _ratingsDataset.getUserRatingsRated(idUser);

                while (userRatingsRated.isEmpty()) {
                    idUser = _users.remove(0);
                    userRatingsRated = _ratingsDataset.getUserRatingsRated(idUser);
                }

                for (Map.Entry<Long, RatingType> entry : userRatingsRated.entrySet()) {
                    _ratings.add(entry.getValue());
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
