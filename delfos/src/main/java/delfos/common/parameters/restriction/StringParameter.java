/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.common.parameters.restriction;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.parameter.StringParameterXML;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Restricción que controla que los valores del parámetro sean del tipo String.
 * Excepcionalmente, permite que los valores sean null.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 * @version 1.2 03-Junio-2013 Ahora admite valores null.
 */
public class StringParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;
    /**
     * Valor de la cadena cuando es null.
     */
    public static final String NULL_VALUE = "_null_";

    /**
     * Constructor por defecto, que asigna el valor por defecto de esta
     * restricción a una cadena vacía.
     */
    public StringParameter() {
        this("");
    }

    /**
     * Constructor por defecto, que asigna un valor por defecto al parámetro que
     * tiene esta restricción.
     *
     * @param defaultValue Valor por defecto para el parametro que incorpore
     * esta restricción.
     */
    public StringParameter(String defaultValue) {
        super(defaultValue);

        if (defaultValue == null) {
            throw new IllegalArgumentException("Default value cannot be null, use " + StringParameter.NULL_VALUE + " instead.");
        }

        if (!isCorrect(defaultValue)) {
            throw new IllegalArgumentException("Default value is not correct.");
        }
    }

    @Override
    public final boolean isCorrect(Object o) {
        if (o == null) {
            return true;
        } else {
            return o instanceof String;
        }
    }

    @Override
    public Object parseString(String parameterValue) {
        return parameterValue;
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return StringParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return StringParameterXML.getStringParameterElement(parameterOwner, parameter);
    }
}
