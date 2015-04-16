package delfos.io.xml.parameterowner.parameter;

import org.jdom2.Element;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.DirectoryParameter;

/**
 * Clase para realizar la entrada/salida a XML para parámetros de directorio de
 * {@link ParameterOwner}
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 25-Julio-2013
 */
public class DirectoryParameterXML {

    /**
     * Genera el elemento XML que describe el parámetro y el valor que tiene.
     *
     * @param parameterOwner Parameter owner al que pertenece al parámetro. Se
     * debe consultar a este objeto para conocer el valor actual del mismo
     * @param p Parámetro a almacenar
     */
    public static Element getDirectoryParameterElement(ParameterOwner parameterOwner, Parameter p) {
        Element parameterElement = new Element(ParameterXML.PARAMETER_ELEMENT_NAME);
        parameterElement.setAttribute(ParameterXML.PARAMETER_NAME, p.getName());

        DirectoryParameter fp = (DirectoryParameter) p.getRestriction();

        parameterElement.setAttribute(ParameterXML.PARAMETER_TYPE, fp.getName());
        parameterElement.setAttribute(ParameterXML.PARAMETER_VALUE, parameterOwner.getParameterValue(p).toString());

//        for (String extension : fp.getAllowedExtensions()) {
//            Element allowedExtension = new Element(EXTENSIONS_ELEMENTS);
//            allowedExtension.setAttribute(ParameterXML.PARAMETER_VALUE, extension);
//            floatParameter.addContent(allowedExtension);
//        }
        return parameterElement;
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
        value = parameterElement.getAttribute(ParameterXML.PARAMETER_VALUE).getValue();
        parameterOwner.setParameterValue(parameter, value);
        return value;
    }
}
