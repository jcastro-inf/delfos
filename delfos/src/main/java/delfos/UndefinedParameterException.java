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
package delfos;

/**
 * Excepción que se lanza cuando se solicitan valores de un parámetro de la
 * linea de comandos y no está definido ({@link ConsoleParameterParser#isDefined(java.lang.String)
 * } devuelve falso)
 *
 * @author Jorge Castro Gallardo
 *
 */
public class UndefinedParameterException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String parameterMissing;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param message Mensaje en el que se puede informar de las causas del
     * error.
     * @param parameterMissing Parámetro que se estaba buscando y no se ha
     * encontrado.
     */
    public UndefinedParameterException(String message, String parameterMissing) {
        super(message + " call ConsoleParameterParser.isDefined(parameter) to check before usage]");
        this.parameterMissing = parameterMissing;
    }

    public String getParameterMissing() {
        return parameterMissing;
    }
}
