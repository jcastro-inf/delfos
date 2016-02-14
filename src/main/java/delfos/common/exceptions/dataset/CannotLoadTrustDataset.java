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
 * Clase que almacena información del error cuando no se puede cargar el dataset
 * de confianza.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 19-Diciembre-2013
 */
public class CannotLoadTrustDataset extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public CannotLoadTrustDataset(String msg) {
        super(msg);
    }

    /**
     * Crea la excepción a partir de otra excepción que describe el error en
     * detalle.
     *
     * @param cause Excepción con el error detallado.
     */
    public CannotLoadTrustDataset(Throwable cause) {
        super(cause);
    }
}
