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
package delfos.dataset.basic.user;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeatures;

/**
 * Interfaz que define los métodos genéricos de un dataset que almacena los
 * datos de los usuarios de un sistema de recomendación.
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public interface UsersDataset extends CollectionOfEntitiesWithFeatures<User> {

    /**
     * Obtiene los datos del usuario especificado.
     *
     * @param idUser
     * @return
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     */
    public User getUser(int idUser) throws UserNotFound;
}
