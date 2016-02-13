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
package delfos.io.xml.predictionprotocol;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.common.parameters.ParameterOwner;

/**
 * Clase para realizar el almacenamiento/recuperación de protocolos de
 * predicción en archivos XML.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
