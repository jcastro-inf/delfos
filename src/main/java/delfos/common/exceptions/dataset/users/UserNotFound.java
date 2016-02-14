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
package delfos.common.exceptions.dataset.users;

import delfos.dataset.basic.user.User;
import delfos.common.exceptions.dataset.entity.EntityNotFound;

/**
 * Excepción que se lanza al intentar buscar un usuario que no existe en el
 * dataset de rating, en el modelo generado por un sistema de recomendación,
 * etc.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 */
public class UserNotFound extends EntityNotFound {

    private static final long serialVersionUID = 1L;
    private final int idUser;

    /**
     * Crea la excepción a partir del id del usuario que no se encuentra.
     *
     * @param idUser Id del usuario no encontrado.
     */
    public UserNotFound(int idUser) {
        super(User.class, idUser, "User '" + idUser + "' not found");
        this.idUser = idUser;
    }

    public UserNotFound(int idUser, Throwable cause) {
        super(User.class, idUser, cause, "User '" + idUser + "' not found");
        this.idUser = idUser;
    }

    public UserNotFound(int idUser, String msg) {
        super(User.class, idUser, msg);
        this.idUser = idUser;
    }

    public UserNotFound(int idUser, Throwable cause, String msg) {
        super(User.class, idUser, cause, msg);
        this.idUser = idUser;
    }

    public int getIdUser() {
        return idUser;
    }
}
