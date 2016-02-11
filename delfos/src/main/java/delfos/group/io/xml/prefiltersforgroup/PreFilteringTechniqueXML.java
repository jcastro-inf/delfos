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
package delfos.group.io.xml.prefiltersforgroup;

import org.jdom2.Element;
import delfos.common.parameters.Parameter;
import delfos.group.factories.GroupRatingsFilterFactory;
import delfos.group.grs.filtered.filters.GroupRatingsFilter;
import delfos.io.xml.parameterowner.parameter.ParameterXML;

/**
 * Clase para realizar la entrada/salida a XML de las técnicas de filtrado de
 * ratings para grupos.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 09-Mayo-2013
 */
public class PreFilteringTechniqueXML {

    public static final String PRE_FILTERING_TECHNIQUE_ELEMENT = "PreFilteringTechnique";

    /**
     * Construye el objeto en memoria que representa a la técnica de filtrado de
     * ratings para grupos descrita en el elemento que se pasa por parámetro.
     *
     * @param groupRatingsPreFilterElement Objeto XML que describe el filtro.
     * @return Técnica de filtrado de ratings para grupo generada a partir de la
     * descripción indicada en el parámetro.
     */
    public static GroupRatingsFilter getGroupRatingsFilter(Element groupRatingsPreFilterElement) {
        String filterName = groupRatingsPreFilterElement.getAttributeValue("name");
        GroupRatingsFilter filter = GroupRatingsFilterFactory.getInstance().getClassByName(filterName);
        for (Object o : groupRatingsPreFilterElement.getChildren(ParameterXML.PARAMETER_ELEMENT_NAME)) {
            Element parameterElement = (Element) o;
            ParameterXML.getParameterValue(filter, parameterElement);
        }
        return filter;
    }

    /**
     * Devuelve el elemento que describe totalmente el filtro, almacenando
     * también los parámetros que posee y su valor.
     *
     * @param groupRatingsPreFilter Filtro de valoraciones a almacenar
     * @return Objeto XML que lo describe
     */
    public static Element getElement(GroupRatingsFilter groupRatingsPreFilter) {
        Element groupRatingsPreFilterElement = new Element(PRE_FILTERING_TECHNIQUE_ELEMENT);
        groupRatingsPreFilterElement.setAttribute("name", groupRatingsPreFilter.getName());

        for (Parameter p : groupRatingsPreFilter.getParameters()) {
            Element parameter = ParameterXML.getElement(groupRatingsPreFilter, p);
            groupRatingsPreFilterElement.addContent(parameter);
        }
        return groupRatingsPreFilterElement;
    }
}
