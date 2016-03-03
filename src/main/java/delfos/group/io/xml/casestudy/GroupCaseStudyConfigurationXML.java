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
package delfos.group.io.xml.casestudy;

import org.jdom2.Element;
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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 UnkowDate
 */
public class GroupCaseStudyConfigurationXML {

    public static final String CASE_ROOT_ELEMENT_NAME = "Recommendations";

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
