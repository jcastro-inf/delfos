package delfos.common.parameters.restriction;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.parameter.BooleanParameterXML;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Clase que representa una restricción sobre parámetros en la que el valor del
 * parámetro debe ser un valor booleano:
 * <code>false</code>,
 * <code>true</code> {@link Boolean#FALSE} ó {@link Boolean#TRUE}
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class BooleanParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;

    /**
     * Constructor de la restricción que asigna un valor por defecto al
     * característica
     *
     * @param defaultValue Valor por defecto del parámetro al que se asigne la
     * restricción
     */
    public BooleanParameter(Boolean defaultValue) {
        super(defaultValue);

        if (!isCorrect(defaultValue)) {
            throw new IllegalArgumentException("Argument isn't correct");
        }
    }

    @Override
    public final boolean isCorrect(Object o) {
        boolean ret = o instanceof Boolean;
        return ret;
    }

    @Override
    public Object parseString(String parameterValue) {
        return Boolean.parseBoolean(parameterValue);
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return BooleanParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return BooleanParameterXML.getBooleanParameterElement(parameterOwner, parameter);
    }
}
