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
package delfos.common.exceptions.dataset;

/**
 * Excepción que se lanza cuando no se puede cargar el dataset de valoraciones.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 * @version 30-Octubre-2013 Ahora es una excepción Unchecked.
 */
public class CannotLoadUsersDataset extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public CannotLoadUsersDataset(String msg) {
        super(msg);
    }

    /**
     * Crea la excepción a partir de otra excepción que describe el error en
     * detalle.
     *
     * @param cause Excepción con el error detallado.
     */
    public CannotLoadUsersDataset(Throwable cause) {
        super(cause);
    }
}
