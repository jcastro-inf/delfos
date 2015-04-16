package delfos.io.xml.predictionprotocol;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.common.parameters.ParameterOwner;

/**
 * Clase para realizar el almacenamiento/recuperación de protocolos de
 * predicción en archivos XML.
 *
* @author Jorge Castro Gallardo
 *
 * @version 15-Noviembre-2013
 */
public class PredictionProtocolXML {

    public static final String ELEMENT_NAME = PredictionProtocol.class.getSimpleName();

    public static Element getElement(PredictionProtocol predictionProtocol) {
        Element element = ParameterOwnerXML.getElement(predictionProtocol);
        element.setName(ELEMENT_NAME);
        return element;
    }

    public static PredictionProtocol getPredictionProtocol(Element predictionProtocolElement) {
        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(predictionProtocolElement);

        if (parameterOwner instanceof PredictionProtocol) {
            return (PredictionProtocol) parameterOwner;
        } else {
            throw new IllegalArgumentException("The object readed is not a prediction protocol.");
        }
    }

}
