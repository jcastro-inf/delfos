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
 * Excepción lanzada cuando se intenta guardar un dataset pero se produce algún
 * fallo que provoca que no sea posible. Ejemplos de fallos son que el archivo
 * en el que se intenta escribir está protegido o que no se encuentra la base de
 * datos en la que se almacena.
 *
* @author Jorge Castro Gallardo
 *
 * @version 18-sep-2013
 * @version 30-Octubre-2013 Ahora es una excepción Unchecked.
 */
public class CannotSaveRatingsDataset extends RuntimeException {

    private static final long serialVersionUID = -3387516993124229948L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param cause Excepción con el error detallado.
     */
    public CannotSaveRatingsDataset(Throwable cause) {
        super(cause);
    }

    /**
     * Crea la excepción a partir de otra excepción que describe el error en
     * detalle.
     *
     * @param message Mensaje a mostrar.
     */
    public CannotSaveRatingsDataset(String message) {
        super(message);
    }
}
