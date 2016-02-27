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
package delfos.io.xml.parameterowner;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.ParameterOwnerType;
import delfos.io.xml.parameterowner.parameter.ParameterXML;
import org.jdom2.Element;

/**
 * Clase para convertir objetos de tipo {@link ParameterOwner} a elementos de
 * XML.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 15-Noviembre-2013
 */
public class ParameterOwnerXML {

    public static final String PARAMETER_OWNER_ELEMENT_NAME = "ParameterOwner";
    public static final String PARAMETER_OWNER_ATTRIBUTE_NAME = "name";
    public static final String PARAMETER_OWNER_ATTRIBUTE_TYPE = "type";

    /**
     * Construye el objeto en memoria que representa al sistema de recomendación
     * descrito en el elemento que se pasa por parámetro.
     *
     * @param parameterOwnerElement Objeto XML con la información para recuperar
     * el objeto.
     *
     * @return Sistema de recomendación generado a partir de la descripción
     * pasada por parámetro
     */
    public static ParameterOwner getParameterOwner(Element parameterOwnerElement) {

        String className = parameterOwnerElement.getAttributeValue(PARAMETER_OWNER_ATTRIBUTE_NAME);
        String typeName = parameterOwnerElement.getAttributeValue(PARAMETER_OWNER_ATTRIBUTE_TYPE);
        ParameterOwnerType parameterOwnerType = ParameterOwnerType.valueOf(typeName);

        ParameterOwner parameterOwner = parameterOwnerType.createObjectFromClassName(className);

        for (Element parameterElement : parameterOwnerElement.getChildren(ParameterXML.PARAMETER_ELEMENT_NAME)) {

            Parameter parameter = parameterOwner.getParameterByName(parameterElement.getAttributeValue(ParameterXML.PARAMETER_NAME));
            Object parameterValue = ParameterXML.getParameterValue(parameterOwner, parameterElement);
            if (parameterValue == null) {
                parameterValue = ParameterXML.getParameterValue(parameterOwner, parameterElement);
            }

            parameterOwner.setParameterValue(parameter, parameterValue);
        }
        return parameterOwner;

    }

    /**
     * Devuelve el elemento que describe totalmente el sistema de recomendación,
     * almacenando también los parámetros que posee y su valor
     *
     * @param parameterOwner Sistema de recomendación a almacenar
     * @return Objeto XML que lo describe
     */
    public static Element getElement(ParameterOwner parameterOwner) {
        Element parameterOwnerElement = new Element("ParameterOwner");
        parameterOwnerElement.setAttribute("name", parameterOwner.getClass().getSimpleName());
        parameterOwnerElement.setAttribute("type", parameterOwner.getParameterOwnerType().name());

        {
            ParameterOwner parameterOwnerAux = parameterOwner.getParameterOwnerType().createObjectFromClassName(parameterOwner.getClass().getSimpleName());
            if (parameterOwnerAux == null) {
                throw new IllegalArgumentException("Cannot create a '" + parameterOwner.getClass().getSimpleName() + "' ParameterOwner from factory");
            }
        }

        for (Parameter p : parameterOwner.getParameters()) {
            Element parameter = ParameterXML.getElement(parameterOwner, p);
            parameterOwnerElement.addContent(parameter);
        }
        return parameterOwnerElement;
    }

}
