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

import java.util.Collection;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 12-Diciembre-2013
 * @param <TrustStatementType>
 */
public interface TrustDataset<TrustStatementType extends TrustStatement> {

    Collection<? extends Long> allUsers();

    Collection<TrustStatementType> getUserTrustStatements(long idUser) throws UserNotFound;

}
