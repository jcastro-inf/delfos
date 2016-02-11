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
package delfos.common.exceptions;

/**
 * Clase que representa la ausencia del archivo de la configuración del sistema
 * de recomendación en la ruta especificada
 *
* @author Jorge Castro Gallardo
 */
public class ConfigurationFileNotFound extends Exception {

    private static final long serialVersionUID = -3387516993124229948L;

    /**
     * Constructor de la excepción que asigna un mensaje de error
     *
     * @param msg Mensaje de la excepción
     */
    public ConfigurationFileNotFound(String msg) {
        super(msg);
    }
}
