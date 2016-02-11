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
package delfos.group.io.xml.grs;

import org.jdom2.Element;
import delfos.ERROR_CODES;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.ParameterOwnerType;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.io.xml.parameterowner.ParameterOwnerXML;

/**
 * Clase para realizar la entrada/salida a XML de los sistemas de recomendación
 * para grupos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 9-Enero-2014
 */
public class GroupRecommenderSystemXML {

    public static final String ELEMENT_NAME = "GroupRecommenderSystem";

    /**
     * Construye el objeto en memoria que representa al sistema de recomendación
     * para grupos descrito en el elemento que se pasa por parámetro.
     *
     * @param grsElement Objeto XML que describe el sistema de recomendación
     * para grupos
     * @return Sistema de recomendación para grupos recuperado del elemento.
     */
    public static GroupRecommenderSystem<Object, Object> getGroupRecommenderSystem(Element grsElement) {

        String name = grsElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_NAME);

        String parameterOwnerType = grsElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE);
        if (parameterOwnerType == null) {
            grsElement.setAttribute(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE, ParameterOwnerType.RECOMMENDER_SYSTEM.name());
        }

        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(grsElement);
        if (parameterOwner instanceof GroupRecommenderSystem) {
            GroupRecommenderSystem groupRecommenderSystem = (GroupRecommenderSystem) parameterOwner;
            return groupRecommenderSystem;
        } else {
            IllegalStateException ex = new IllegalStateException("The XML does not have the expected structure: The loaded parameter owner is not a group recommender system [" + parameterOwner + "]");
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
            throw ex;
        }
    }

    /**
     * Devuelve el elemento que describe totalmente el sistema de recomendación,
     * almacenando también los parámetros que posee y su valor
     *
     * @param grs Sistema de recomendación a almacenar
     * @return Objeto XML que lo describe
     */
    public static Element getElement(GroupRecommenderSystem<Object, Object> grs) {
        Element element = ParameterOwnerXML.getElement(grs);
        element.setName(ELEMENT_NAME);
        return element;
    }
}
