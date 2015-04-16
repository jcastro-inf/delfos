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
* @author Jorge Castro Gallardo
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
