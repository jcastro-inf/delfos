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
