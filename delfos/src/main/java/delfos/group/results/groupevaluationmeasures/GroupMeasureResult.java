package delfos.group.results.groupevaluationmeasures;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 * Almacena los resultados de una métrica de evaluación
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (28 Octubre 2012)
 */
public class GroupMeasureResult {

    private final Element element;
    private final double value;
    private final GroupEvaluationMeasure groupEvaluationMeasure;
    private final Object detailedResult;

    public GroupMeasureResult(GroupEvaluationMeasure groupEvaluationMeasure, double value, Element element, Object detailedResult) {
        this.element = element;
        this.value = value;
        this.groupEvaluationMeasure = groupEvaluationMeasure;
        this.detailedResult = detailedResult;
    }

    public GroupMeasureResult(GroupEvaluationMeasure groupEvaluationMeasure, double value, Element element) {
        this(groupEvaluationMeasure, value, element, null);
    }

    public GroupMeasureResult(GroupEvaluationMeasure groupEvaluationMeasure, double value) {
        this.groupEvaluationMeasure = groupEvaluationMeasure;
        element = ParameterOwnerXML.getElement(groupEvaluationMeasure);
        element.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(value));
        this.value = value;
        this.detailedResult = null;
    }

    public Element getXMLElement() {
        return element;
    }

    public double getValue() {
        return value;
    }

    public GroupEvaluationMeasure getGroupEvaluationMeasure() {
        return groupEvaluationMeasure;
    }

    @Override
    public String toString() {
        return groupEvaluationMeasure.getName() + " : " + getValue();
    }

    public boolean hasDetailedObject() {
        return detailedResult != null;
    }

    public Object getDetailedResult() {
        if (detailedResult == null) {
            throw new IllegalStateException("This result does not have a detailed result explanation.");
        }
        return detailedResult;
    }

}
