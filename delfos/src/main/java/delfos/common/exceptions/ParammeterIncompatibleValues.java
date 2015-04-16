package delfos.common.exceptions;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Excepción que se lanza cuando se detecta que el valor de dos parámetros de un
 * {@link ParameterOwner} son incompatibles.
 *
* @author Jorge Castro Gallardo
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
