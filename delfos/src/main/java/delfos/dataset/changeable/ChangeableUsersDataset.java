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
package delfos.dataset.changeable;

import delfos.common.exceptions.dataset.CannotSaveUsersDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public interface ChangeableUsersDataset extends UsersDataset {

    /**
     * Añade el usuario especificado. Si el usuario ya existía, lanza una
     * excepción.
     *
     * @param user Información del usuario a agregar.
     */
    public void addUser(User user);

    /**
     * Ordena que los datos sean guardados en el método persistente
     * correspondiente del dataset.
     */
    public void commitChangesInPersistence() throws CannotSaveUsersDataset;
}
