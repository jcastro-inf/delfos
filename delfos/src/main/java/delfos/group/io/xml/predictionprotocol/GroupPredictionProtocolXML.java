package delfos.group.io.xml.predictionprotocol;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.common.parameters.ParameterOwner;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;

/**
 * Clase para realizar el almacenamiento/recuperación de protocolos de
 * predicción para grupos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 9-Enero-2014
 */
public class GroupPredictionProtocolXML {

    public static final String ELEMENT_NAME = GroupPredictionProtocol.class.getSimpleName();

    public static Element getElement(GroupPredictionProtocol validationTechnique) {
        Element element = ParameterOwnerXML.getElement(validationTechnique);
        element.setName(ELEMENT_NAME);
        return element;
    }

    public static GroupPredictionProtocol getGroupPredictionProtocol(Element validationTechniqueElement) {
        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(validationTechniqueElement);

        if (parameterOwner instanceof GroupPredictionProtocol) {
            return (GroupPredictionProtocol) parameterOwner;
        } else {
            throw new IllegalArgumentException("The object readed is not a validation technique.");
        }
    }

}
