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
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.CannotParseParameterValue;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 13-Mayo-2013
 */
public class ParameterOwnerParameterXML {

    /**
     * Devuelve el elemento que describe totalmente el ParameterOwner,
     * almacenando también los parámetros que posee y su valor
     *
     * @param parameterOwner ParameterOwner a almacenar.
     * @param parameter Parámetro en que se encuentra.
     * @return Objeto XML que lo describe
     */
    public static Element getParameterOwnerElement(ParameterOwner parameterOwner, Parameter parameter) {

        if (parameterOwner == null) {
            throw new IllegalArgumentException("The parameterOwner is null.");
        }

        Element ret = new Element(ParameterXML.PARAMETER_ELEMENT_NAME);
        ret.setAttribute(ParameterXML.PARAMETER_NAME, parameter.getName());

        ParameterOwner innerParameterOwner = (ParameterOwner) parameterOwner.getParameterValue(parameter);
        if (parameter != null) {
            ret.setAttribute(ParameterXML.PARAMETER_TYPE, ParameterOwnerRestriction.class.getSimpleName());
            ret.setAttribute(ParameterXML.PARAMETER_VALUE, innerParameterOwner.getName());
        } else {
            ret.setAttribute(ParameterXML.PARAMETER_TYPE, innerParameterOwner.getName());
            ret.setAttribute(ParameterXML.PARAMETER_VALUE, innerParameterOwner.getName());
        }

        for (Parameter innerParameter : innerParameterOwner.getParameters()) {
            Element innerParameterElement = ParameterXML.getElement(innerParameterOwner, innerParameter);
            ret.addContent(innerParameterElement);
        }
        return ret;
    }

    public static ParameterOwner getParameterOwnerParameterValue(ParameterOwner parameterOwner, Element parameterElement) {

        if (parameterElement == null) {
            throw new IllegalArgumentException("The element cannot be null.");
        }

        if (!parameterElement.getName().equals(ParameterXML.PARAMETER_ELEMENT_NAME)) {
            throw new IllegalArgumentException("The element does not describe a parameter.");
        }

        if (!parameterElement.getAttributeValue(ParameterXML.PARAMETER_TYPE).equals(ParameterOwnerRestriction.class.getSimpleName())) {
            throw new IllegalArgumentException("The element does not describe a parameter of type " + ParameterOwnerRestriction.class);
        }

        String parameterName = parameterElement.getAttributeValue(ParameterXML.PARAMETER_NAME);
        Parameter parameterByName = parameterOwner.getParameterByName(parameterName);
        if (parameterByName == null) {
            IllegalStateException ex = new IllegalStateException(parameterOwner.getName() + " doesn't have the parameter '" + parameterName + "'\n");
            ERROR_CODES.PARAMETER_OWNER_NOT_HAVE_PARAMETER.exit(ex);
            throw ex;
        }

        String parameterValueString = parameterElement.getAttributeValue(ParameterXML.PARAMETER_VALUE);

        ParameterOwner innerParameterOwner = null;
        try {
            innerParameterOwner = (ParameterOwner) parameterByName.parseString(parameterValueString);
        } catch (ClassCastException ex) {
            throw new IllegalStateException("El tipo del parametro no es el detectado!!");
        } catch (CannotParseParameterValue ex) {
            ERROR_CODES.PARAMETER_OWNER_ILLEGAL_PARAMETER_VALUE.exit(ex);
            throw new IllegalStateException(ex);
        }

        if (innerParameterOwner == null) {
            throw new IllegalStateException("Parameter owner '" + parameterValueString + "' not found.");
        }

        for (Element innerParameterElement : parameterElement.getChildren(ParameterXML.PARAMETER_ELEMENT_NAME)) {
            String innerParameterName = innerParameterElement.getAttributeValue(ParameterXML.PARAMETER_NAME);

            Parameter innerParameter = innerParameterOwner.getParameterByName(innerParameterName);
            Object innerParameterValue = ParameterXML.getParameterValue(innerParameterOwner, innerParameterElement);
            innerParameterOwner.setParameterValue(innerParameter, innerParameterValue);
        }
        return innerParameterOwner;
    }
}
