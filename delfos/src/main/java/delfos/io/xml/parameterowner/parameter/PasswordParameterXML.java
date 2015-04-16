package delfos.io.xml.parameterowner.parameter;

import org.jdom2.Element;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.PasswordParameter;

/**
 * Clase para realizar la entrada/salida a XML para parámetros reales de
 * {@link ParameterOwner}
 *
 * @version 1.0 (06/12/2012)
 *
* @author Jorge Castro Gallardo
 */
public class PasswordParameterXML {

    /**
     * Nombre de la característica en que se almacena el valor del parámetro
     */
    public static String VALUE_ATTRIBUTE = "parameterValue";

    /**
     * Genera el elemento XML que describe el parámetro y el valor que tiene.
     *
     * @param parameterOwner Parameter owner al que pertenece al parámetro. Se
     * debe consultar a este objeto para conocer el valor actual del mismo
     * @param p Parámetro a almacenar
     */
    public static Element getPasswordParameterElement(ParameterOwner parameterOwner, Parameter p) {
        Element floatParameter = new Element(ParameterXML.PARAMETER_ELEMENT_NAME);
        floatParameter.setAttribute(ParameterXML.PARAMETER_NAME, p.getName());

        PasswordParameter pp = (PasswordParameter) p.getRestriction();

        floatParameter.setAttribute(ParameterXML.PARAMETER_TYPE, pp.getName());
        floatParameter.setAttribute(VALUE_ATTRIBUTE, parameterOwner.getParameterValue(p).toString());

        return floatParameter;
    }

    /**
     * Asigna el valor del parámetro especificado en el objeto XML al
     * {@link ParameterOwner} especificado
     *
     * @param parameterOwner Objeto al que asignar el parámetro
     * @param parameterElement Elemento que describe el parámetro y su valor
     * @return Valor del parámetro. Si ha habido algun error, devuelve null
     */
    public static Object getParameterValue(ParameterOwner parameterOwner, Element parameterElement) {
        String parameterName = parameterElement.getAttributeValue(ParameterXML.PARAMETER_NAME);

        Parameter parameter = parameterOwner.getParameterByName(parameterName);
        if (parameter == null) {
            Global.showWarning(parameterOwner.getName() + " doesn't have the parameter '" + parameterName + "'\n");
        }

        Object value;
        value = parameterElement.getAttribute(VALUE_ATTRIBUTE).getValue();
        parameterOwner.setParameterValue(parameter, value);
        return value;
    }
}
