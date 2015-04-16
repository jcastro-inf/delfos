package delfos.group.io.xml.groupformationtechnique;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.common.parameters.ParameterOwner;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;

/**
 * Clase para realizar el almacenamiento/recuperación de técnicas de generación
 * de grupos.
 *
* @author Jorge Castro Gallardo
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
