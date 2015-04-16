package delfos.io.xml.recommendationcandidatesselector;

import org.jdom2.Element;
import delfos.ERROR_CODES;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.ParameterOwnerType;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.io.xml.parameterowner.parameter.ParameterXML;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 3-Junio-2014
 */
public class RecommendationCandidatesSelectorXML {

    public static final String RECOMMENDATION_CANDIDATE_SELECTOR_ELEMENT_NAME = "RecommendationCandidateSelector";

    public static Element getElement(RecommendationCandidatesSelector recommendationCandidatesSelector) {
        Element recommendationsOutputMethodElement = new Element(RECOMMENDATION_CANDIDATE_SELECTOR_ELEMENT_NAME);

        recommendationsOutputMethodElement.setAttribute(
                ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_NAME,
                recommendationCandidatesSelector.getName());

        recommendationsOutputMethodElement.setAttribute(
                ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE,
                ParameterOwnerType.RECOMMENDATION_CANDIDATES_SELECTOR.name());

        for (Parameter p : recommendationCandidatesSelector.getParameters()) {
            Element parameter = ParameterXML.getElement(recommendationCandidatesSelector, p);
            recommendationsOutputMethodElement.addContent(parameter);
        }
        return recommendationsOutputMethodElement;
    }

    public static RecommendationCandidatesSelector getRecommendationsCandidatesSelector(Element recommendationCandidatesSelectorElement) {
        String name = recommendationCandidatesSelectorElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_NAME);
        String parameterOwnerType = recommendationCandidatesSelectorElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE);
        if (parameterOwnerType == null) {
            recommendationCandidatesSelectorElement.setAttribute(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE, ParameterOwnerType.RECOMMENDATIONS_OUTPUT_METHOD.name());
        }
        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(recommendationCandidatesSelectorElement);
        if (parameterOwner instanceof RecommendationCandidatesSelector) {
            RecommendationCandidatesSelector recommendationCandidatesSelector = (RecommendationCandidatesSelector) parameterOwner;
            return recommendationCandidatesSelector;
        } else {
            IllegalStateException ex = new IllegalStateException("The XML does not have the expected structure: The loaded parameter owner is not a recommendation candidate selector[" + parameterOwner + "]");
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
            throw ex;
        }
    }
}
