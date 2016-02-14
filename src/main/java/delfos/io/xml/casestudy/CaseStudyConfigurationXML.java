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
package delfos.io.xml.casestudy;

import org.jdom2.Element;
import delfos.experiment.casestudy.CaseStudyConfiguration;
import static delfos.io.xml.casestudy.CaseStudyXML.CASE_ROOT_ELEMENT_NAME;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.io.xml.dataset.RelevanceCriteriaXML;
import delfos.io.xml.predictionprotocol.PredictionProtocolXML;
import delfos.io.xml.rs.RecommenderSystemXML;
import delfos.io.xml.validationtechnique.ValidationTechniqueXML;

/**
 * Clase encargada de hacer la entrada/salida de los resultados de la ejeución
 * de un caso de uso concreto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 UnkowDate
 */
public class CaseStudyConfigurationXML {

    public static Element caseStudyConfigurationToXMLElement(CaseStudyConfiguration caseStudyConfiguration) {

        Element caseStudyConfigurationElement = new Element(CASE_ROOT_ELEMENT_NAME);

        caseStudyConfigurationElement.addContent(RecommenderSystemXML.getElement(caseStudyConfiguration.getRecommenderSystem()));
        caseStudyConfigurationElement.addContent(ValidationTechniqueXML.getElement(caseStudyConfiguration.getValidationTechnique()));
        caseStudyConfigurationElement.addContent(PredictionProtocolXML.getElement(caseStudyConfiguration.getPredictionProtocol()));

        caseStudyConfigurationElement.addContent(RelevanceCriteriaXML.getElement(caseStudyConfiguration.getRelevanceCriteria()));
        caseStudyConfigurationElement.addContent(DatasetLoaderXML.getElement(caseStudyConfiguration.getDatasetLoader()));

        return caseStudyConfigurationElement;

    }
}
