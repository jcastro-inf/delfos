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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 12-Diciembre-2013
 *
 * @param <TrustStatementType> Clase que almacena toda la información del
 * registro de confianza.
 */
public class TrustDatasetAbstract<TrustStatementType extends TrustStatement> implements TrustDataset<TrustStatementType> {

    private final Map<Integer, Map<Integer, TrustStatementType>> trustsByUserWhoStates;

    private final Set<Integer> allUsers;

    public TrustDatasetAbstract(Collection<? extends TrustStatementType> trustStatements) {
        this.allUsers = new TreeSet<>();
        this.trustsByUserWhoStates = new TreeMap<>();

        for (TrustStatementType trustStatement : trustStatements) {
            int idUser = trustStatement.idUserSource;
            int idNeighbor = trustStatement.idUserDestiny;

            if (!trustsByUserWhoStates.containsKey(idUser)) {
                trustsByUserWhoStates.put(idUser, new TreeMap<>());
            }

            allUsers.add(idUser);
            allUsers.add(idNeighbor);

            trustsByUserWhoStates.get(idUser).put(idNeighbor, trustStatement);
        }

    }

    @Override
    public Collection<TrustStatementType> getUserTrustStatements(int idUser) throws UserNotFound {
        return new ArrayList<TrustStatementType>(trustsByUserWhoStates.get(idUser).values());
    }

    @Override
    public Collection<? extends Integer> allUsers() {
        return new ArrayList<Integer>(allUsers);
    }
}
