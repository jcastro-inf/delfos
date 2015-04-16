package delfos.group.io.xml.casestudy;

import org.jdom2.Element;
import static delfos.group.GroupRecommendationManager.CASE_ROOT_ELEMENT_NAME;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.io.xml.groupformationtechnique.GroupFormationTechniqueXML;
import delfos.group.io.xml.predictionprotocol.GroupPredictionProtocolXML;
import delfos.group.io.xml.validationtechnique.GroupValidationTechniqueXML;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.io.xml.dataset.RelevanceCriteriaXML;
import delfos.io.xml.rs.RecommenderSystemXML;

/**
 * Clase encargada de hacer la entrada/salida de los resultados de la ejeución
 * de un caso de uso concreto.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 UnkowDate
 */
public class GroupCaseStudyConfigurationXML {

    public static Element caseStudyConfigurationToXMLElement(GroupCaseStudyConfiguration groupCaseStudyConfiguration) {

        Element groupCaseStudyConfigurationElement = new Element(CASE_ROOT_ELEMENT_NAME);

        groupCaseStudyConfigurationElement.addContent(RecommenderSystemXML.getElement(groupCaseStudyConfiguration.getGroupRecommenderSystem()));

        groupCaseStudyConfigurationElement.addContent(GroupFormationTechniqueXML.getElement(groupCaseStudyConfiguration.getGroupFormationTechnique()));
        groupCaseStudyConfigurationElement.addContent(GroupValidationTechniqueXML.getElement(groupCaseStudyConfiguration.getGroupValidationTechnique()));
        groupCaseStudyConfigurationElement.addContent(GroupPredictionProtocolXML.getElement(groupCaseStudyConfiguration.getGroupPredictionProtocol()));

        groupCaseStudyConfigurationElement.addContent(RelevanceCriteriaXML.getElement(groupCaseStudyConfiguration.getRelevanceCriteria()));
        groupCaseStudyConfigurationElement.addContent(DatasetLoaderXML.getElement(groupCaseStudyConfiguration.getDatasetLoader()));

        return groupCaseStudyConfigurationElement;

    }

}
