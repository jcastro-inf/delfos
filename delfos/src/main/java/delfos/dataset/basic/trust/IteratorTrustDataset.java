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
package delfos.dataset.basic.trust;

import java.util.Iterator;
import java.util.LinkedList;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Clase para iterar de forma genérica sobre un dataset de valores de confianza.
 *
* @author Jorge Castro Gallardo
 *
 * @version 18-Diciembre-2013
 * @param <TrustStatementType>
 */
public class IteratorTrustDataset<TrustStatementType extends TrustStatement> implements Iterator<TrustStatementType> {

    private TrustStatementType _next;
    private final LinkedList<Integer> _users;
    private final LinkedList<TrustStatementType> _trusts;
    private final TrustDatasetAbstract<TrustStatementType> _trustDataset;
    private final Object exMut = 0;

    /**
     * Crea el iterador para recorrer todos los ratings del dataset indicado.
     *
     * @param trustDataset
     */
    public IteratorTrustDataset(TrustDatasetAbstract<TrustStatementType> trustDataset) {
        _users = new LinkedList<Integer>(trustDataset.allUsers());
        _trustDataset = trustDataset;
        _trusts = new LinkedList<TrustStatementType>();
        loadNextRating();
    }

    @Override
    public boolean hasNext() {
        synchronized (exMut) {
            return _next != null;
        }
    }

    @Override
    public TrustStatementType next() {
        synchronized (exMut) {
            TrustStatementType ret = _next;

            loadNextRating();

            return ret;
        }
    }

    @Override
    public void remove() {
        throw new IllegalStateException("Not allowed method.");
    }

    private void loadNextRating() {
        if (_trusts.isEmpty()) {
            //Lista vacía, cargar siguientes ratings.

            if (_users.isEmpty()) {
                ///No hay más usuarios, finalizar.
                _next = null;
            } else {
                //Hay mas usuarios, cargar sus ratings.
                int idUser = _users.remove(0);
                try {
                    for (TrustStatementType trustStatement : _trustDataset.getUserTrustStatements(idUser)) {
                        _trusts.add(trustStatement);
                    }
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }

                //Lista cargada, preparar el siguiente rating.
                _next = _trusts.remove(0);
            }
        } else {
            //La lista de ratings no está vacía, preparar siguiente rating
            _next = _trusts.remove(0);
        }
    }

}
