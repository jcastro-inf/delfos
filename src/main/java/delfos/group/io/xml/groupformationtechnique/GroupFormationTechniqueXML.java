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
package delfos.group.io.xml.groupformationtechnique;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.common.parameters.ParameterOwner;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;

/**
 * Clase para realizar el almacenamiento/recuperación de técnicas de generación
 * de grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 9-Enero-2014
 */
public class GroupFormationTechniqueXML {

    public static final String ELEMENT_NAME = GroupFormationTechnique.class.getSimpleName();

    public static Element getElement(GroupFormationTechnique validationTechnique) {
        Element element = ParameterOwnerXML.getElement(validationTechnique);
        element.setName(ELEMENT_NAME);
        return element;
    }

    public static GroupFormationTechnique getGroupFormationTechnique(Element validationTechniqueElement) {
        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(validationTechniqueElement);

        if (parameterOwner instanceof GroupFormationTechnique) {
            return (GroupFormationTechnique) parameterOwner;
        } else {
            throw new IllegalArgumentException("The object readed is not a " + GroupFormationTechnique.class + ".");
        }
    }

}
