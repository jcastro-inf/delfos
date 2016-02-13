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

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Excepción que se lanza cuando se detecta que el valor de dos parámetros de un
 * {@link ParameterOwner} son incompatibles.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 9-Mayo-2013
 */
public class ParammeterIncompatibleValues extends Exception {

    private final static long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public ParammeterIncompatibleValues(String msg) {
        super(msg);
    }

    /**
     * Crea la excepción a partir de los parámetros que son incompatibles.
     */
    public ParammeterIncompatibleValues(ParameterOwner po, Parameter a, Parameter b) {
        super("Parameter values of " + po.getName() + " are incompatible: \n"
                + a.getName() + " = " + po.getParameterValue(a) + "\n"
                + b.getName() + " = " + po.getParameterValue(b) + "\n");
    }
}
