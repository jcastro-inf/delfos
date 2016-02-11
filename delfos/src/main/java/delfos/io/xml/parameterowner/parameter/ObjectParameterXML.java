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
package delfos.io.xml.parameterowner.parameter;

import org.jdom2.Element;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.CannotParseParameterValue;
import delfos.common.parameters.restriction.ObjectParameter;

/**
 * Clase para realizar la entrada/salida a XML para parámetros reales de
 * {@link ParameterOwner}
 *
 * @version 1.0 (06/12/2012)
 *
* @author Jorge Castro Gallardo
 */
public class ObjectParameterXML {

    /**
     * Nombre de la característica en que se almacena el valor del parámetro
     */
    public static String VALUE_ATTRIBUTE = "parameterValue";
    public static String ALLOWED_ELEMENTS = "allowedValues";

    /**
     * Genera el elemento XML que describe el parámetro y el valor que tiene.
     *
     * @param parameterOwner Parameter owner al que pertenece al parámetro. Se
     * debe consultar a este objeto para conocer el valor actual del mismo
     * @param p Parámetro a almacenar
     */
    static Element getObjectParameterElement(ParameterOwner parameterOwner, Parameter p) {
        Element objectParameterElement = new Element(ParameterXML.PARAMETER_ELEMENT_NAME);
        objectParameterElement.setAttribute(ParameterXML.PARAMETER_NAME, p.getName());

        ObjectParameter op = (ObjectParameter) p.getRestriction();

        objectParameterElement.setAttribute(ParameterXML.PARAMETER_TYPE, op.getName());

        Object parameterValue = parameterOwner.getParameterValue(p);

        objectParameterElement.setAttribute(VALUE_ATTRIBUTE, parameterValue.toString());

        if (Global.isVerboseAnnoying()) {
            for (Object allowed : op.getAllowed()) {
                Element allowedElement = new Element(ALLOWED_ELEMENTS);
                allowedElement.setAttribute(VALUE_ATTRIBUTE, allowed.toString());
                objectParameterElement.addContent(allowedElement);
            }
        }

        return objectParameterElement;
    }

    /**
     * Asigna el valor del parámetro especificado en el objeto XML al
     * {@link ParameterOwner} especificado
     *
     * @param parameterOwner Objeto al que asignar el parámetro
     * @param parameterElement Elemento que describe el parámetro y su valor
     * @return Valor del parámetro. Si ha habido algun error, devuelve null
     */
    static Object getParameterValue(ParameterOwner parameterOwner, Element parameterElement) {
        String parameterName = parameterElement.getAttributeValue(ParameterXML.PARAMETER_NAME);

        Parameter parameter = parameterOwner.getParameterByName(parameterName);
        if (parameter == null) {
            IllegalStateException ex = new IllegalStateException(parameterOwner.getName() + " doesn't have the parameter '" + parameterName + "'\n");
            ERROR_CODES.PARAMETER_OWNER_NOT_HAVE_PARAMETER.exit(ex);
            throw ex;
        }

        String featureValue;
        featureValue = parameterElement.getAttribute(VALUE_ATTRIBUTE).getValue();

        Object parameterValue;
        try {
            parameterValue = parameter.parseString(featureValue);
        } catch (CannotParseParameterValue ex) {
            ERROR_CODES.PARAMETER_OWNER_ILLEGAL_PARAMETER_VALUE.exit(ex);
            throw new IllegalStateException(ex);
        }
        parameterOwner.setParameterValue(parameter, parameterValue);
        return parameterOwner.getParameterValue(parameter);
    }
}
