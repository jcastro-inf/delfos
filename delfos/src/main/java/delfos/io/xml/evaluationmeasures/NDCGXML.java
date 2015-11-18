package delfos.io.xml.evaluationmeasures;

import delfos.Constants;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.NDCG;
import java.util.Collections;
import java.util.List;
import org.jdom2.Element;

/**
 * @author Jorge Castro Gallardo
 *
 * @version 9-sept-2014
 */
public class NDCGXML {

    public static final String NDCG_ELEMENT_NAME = "NDCG";

    public static final String NDCG_MIN_ATTRIBUTE_NAME = "min";
    public static final String NDCG_PERCENTILE_25_ATTRIBUTE_NAME = "percentile25";
    public static final String NDCG_MEAN_ATTRIBUTE_NAME = "mean";
    public static final String NDCG_PERCENTILE_75_ATTRIBUTE_NAME = "percentile75";
    public static final String NDCG_MAX_ATTRIBUTE_NAME = "max";

    public static final String NDCG_VALUES_ELEMENT_NAME = "ndcg_values";
    public static final String NDCG_VALUE_ELEMENT_NAME = "ndcg_value";
    public static final String NDCG_VALUE_ATTRIBUTE_NAME = "value";

    public static Element getElement(List<Double> ndcgValues) {
        Element element = ParameterOwnerXML.getElement(new NDCG());

        Collections.sort(ndcgValues);
        double min = ndcgValues.get(0);
        double percentile25 = ndcgValues.get((int) (ndcgValues.size() * 0.25));
        double mean = new MeanIterative(ndcgValues).getMean();
        double percentile75 = ndcgValues.get((int) (ndcgValues.size() * 0.75));
        double max = ndcgValues.get(ndcgValues.size() - 1);

        element.setAttribute(NDCG_MIN_ATTRIBUTE_NAME, Double.toString(min));
        element.setAttribute(NDCG_PERCENTILE_25_ATTRIBUTE_NAME, Double.toString(percentile25));
        element.setAttribute(NDCG_MEAN_ATTRIBUTE_NAME, Double.toString(mean));
        element.setAttribute(NDCG_PERCENTILE_75_ATTRIBUTE_NAME, Double.toString(percentile75));
        element.setAttribute(NDCG_MAX_ATTRIBUTE_NAME, Double.toString(max));
        element.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(mean));

        if (Constants.isRawResultDefined()) {
            Element ndcgValuesElement = new Element("ndcg_values");
            for (double value : ndcgValues) {
                Element ndcgValueElement = new Element(NDCG_VALUE_ELEMENT_NAME);
                ndcgValueElement.setAttribute(NDCG_VALUE_ELEMENT_NAME, Double.toString(value));
                ndcgValuesElement.addContent(ndcgValueElement);
            }
            element.addContent(ndcgValuesElement);
        }
        return element;
    }

    public static List<Double> getNDCGPerUser(Element ndcgElement) {
        if (!NDCG_ELEMENT_NAME.equals(ndcgElement.getName())) {
            throw new IllegalStateException("Passed element is not a NDCG result element");
        }

        throw new IllegalStateException("Implement this method.");
    }
}
