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
package delfos.io.xml.rs;

import org.jdom2.Element;
import delfos.ERROR_CODES;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.rs.GenericRecommenderSystem;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Clase para realizar la entrada/salida a XML de los sistemas de recomendación.
 *
 * <p>
 * <p>
 * Version 1.1: Se establecen los métodos de entrada/salida a XML.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class RecommenderSystemXML {

    public static final String ELEMENT_NAME = "RecommenderSystem";

    /**
     * Construye el objeto en memoria que representa al sistema de recomendación
     * descrito en el elemento que se pasa por parámetro.
     *
     * @param rsElement Objeto XML que describe el sistema de recomendación
     * @return Sistema de recomendación generado a partir de la descripción
     * pasada por parámetro
     */
    public static GenericRecommenderSystem<Object> getRecommenderSystem(Element rsElement) {

        String name = rsElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_NAME);

        String parameterOwnerType = rsElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE);
        if (parameterOwnerType == null) {
            rsElement.setAttribute(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE, ParameterOwnerType.RECOMMENDER_SYSTEM.name());
        }

        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(rsElement);

        if (parameterOwner == null) {
            IllegalStateException ex = new IllegalStateException("No recommender system named '" + name + "' found.");
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
            throw ex;
        } else {
            if (parameterOwner instanceof GenericRecommenderSystem) {
                GenericRecommenderSystem genericRecommenderSystem = (GenericRecommenderSystem) parameterOwner;
                return genericRecommenderSystem;
            } else {
                IllegalStateException ex = new IllegalStateException("The XML does not have the expected structure: The loaded parameter owner is not a recommender system [" + parameterOwner + "]");
                ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
                throw ex;
            }
        }
    }

    /**
     * Devuelve el elemento que describe totalmente el sistema de recomendación,
     * almacenando también los parámetros que posee y su valor
     *
     * @param rs Sistema de recomendación a almacenar
     * @return Objeto XML que lo describe
     */
    public static Element getElement(GenericRecommenderSystem<? extends Object> rs) {
        Element element = ParameterOwnerXML.getElement(rs);
        element.setName(ELEMENT_NAME);
        return element;
    }
}
