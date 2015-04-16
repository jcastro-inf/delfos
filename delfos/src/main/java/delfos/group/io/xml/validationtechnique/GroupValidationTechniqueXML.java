package delfos.group.io.xml.validationtechnique;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.common.parameters.ParameterOwner;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;

/**
 * Clase para realizar el almacenamiento/recuperación de técnicas de validación
 * en archivos XML.
 *
* @author Jorge Castro Gallardo
 *
 * @version 9-Enero-2014
 */
public class GroupValidationTechniqueXML {

    public static final String ELEMENT_NAME = GroupValidationTechnique.class.getSimpleName();

    public static Element getElement(GroupValidationTechnique validationTechnique) {
        Element element = ParameterOwnerXML.getElement(validationTechnique);
        element.setName(ELEMENT_NAME);
        return element;
    }

    public static GroupValidationTechnique getGroupValidationTechnique(Element validationTechniqueElement) {
        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(validationTechniqueElement);

        if (parameterOwner instanceof GroupValidationTechnique) {
            return (GroupValidationTechnique) parameterOwner;
        } else {
            throw new IllegalArgumentException("The object readed is not a validation technique.");
        }
    }

}
