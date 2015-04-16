package delfos.io.xml.validationtechnique;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.common.parameters.ParameterOwner;

/**
 * Clase para realizar el almacenamiento/recuperación de técnicas de validación
 * en archivos XML.
 *
* @author Jorge Castro Gallardo
 *
 * @version 15-Noviembre-2013
 */
public class ValidationTechniqueXML {

    public static final String ELEMENT_NAME = ValidationTechnique.class.getSimpleName();

    public static Element getElement(ValidationTechnique validationTechnique) {
        Element element = ParameterOwnerXML.getElement(validationTechnique);
        element.setName(ELEMENT_NAME);
        return element;
    }

    public static ValidationTechnique getValidationTechnique(Element validationTechniqueElement) {
        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(validationTechniqueElement);

        if (parameterOwner instanceof ValidationTechnique) {
            return (ValidationTechnique) parameterOwner;
        } else {
            throw new IllegalArgumentException("The object readed is not a validation technique.");
        }
    }

}
