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

import java.io.File;

/**
 * Excepción que se lanza cuando no se encuentra el archivo que almacena el
 * modelo generado por un sistema de recomendación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 */
public class ModelFileNotFound extends Exception {

    private final static long serialVersionUID = 1L;

    /**
     * Excepción que se lanza cuando no se encuentra el archivo que contiene el
     * modelo de recomendación.
     *
     * @param msg Mensaje que describe el error.
     */
    public ModelFileNotFound(String msg) {
        super(msg);
    }

    /**
     * Excepción que se lanza cuando no se encuentra el archivo que contiene el
     * modelo de recomendación.
     *
     * @param modelFile Fichero no encontrado.
     */
    public ModelFileNotFound(File modelFile) {
        super("Model file '" + modelFile.getAbsolutePath() + "' not found.");
    }
}
