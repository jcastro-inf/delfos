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
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.IntegerParameter;

/**
 * Clase para realizar la entrada/salida a XML para parámetros enteros de
 * {@link ParameterOwner}
 *
 * @version 1.0 (06/12/2012)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class IntegerParameterXML {

    /**
     * Nombre de la característica en que se guarda el valor mínimo del
     * parámetro
     */
    public static String MIN_VALUE_ATTRIBUTE = "minValue";
    /**
     * Nombre de la característica en que se guarda el valor máximo del
     * parámetro
     */
    public static String MAX_VALUE_ATTRIBUTE = "maxValue";
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
    public static Element getIntegerParameterElement(ParameterOwner parameterOwner, Parameter p) {
        Element integerParameter = new Element(ParameterXML.PARAMETER_ELEMENT_NAME);
        integerParameter.setAttribute(ParameterXML.PARAMETER_NAME, p.getName());

        IntegerParameter ip = (IntegerParameter) p.getRestriction();
        integerParameter.setAttribute(ParameterXML.PARAMETER_TYPE, ip.getName());
        if (Global.isVerboseAnnoying()) {
            integerParameter.setAttribute(MIN_VALUE_ATTRIBUTE, Integer.toString(ip.getMin()));
            integerParameter.setAttribute(MAX_VALUE_ATTRIBUTE, Integer.toString(ip.getMax()));
        }
        integerParameter.setAttribute(VALUE_ATTRIBUTE, parameterOwner.getParameterValue(p).toString());

        return integerParameter;
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
        parameterOwner.setParameterValue(parameter, Integer.parseInt(value.toString()));
        return value;
    }
}
