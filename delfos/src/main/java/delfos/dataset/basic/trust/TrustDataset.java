package delfos.dataset.basic.trust;

import java.util.Collection;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 12-Diciembre-2013
 * @param <TrustStatementType>
 */
public interface TrustDataset<TrustStatementType extends TrustStatement> {

    Collection<? extends Integer> allUsers();

    Collection<TrustStatementType> getUserTrustStatements(int idUser) throws UserNotFound;

}
