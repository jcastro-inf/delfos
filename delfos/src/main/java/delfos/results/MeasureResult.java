package delfos.results;

import org.jdom2.Element;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 * Almacena los resultados de una métrica de evaluación
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class MeasureResult {

    private final Element element;
    private final float value;
    private final EvaluationMeasure evaluationMeasure;
    private final Object detailedResult;

    public MeasureResult(EvaluationMeasure evaluationMeasure, float value, Element element) {
        this(evaluationMeasure, value, element, null);
    }

    public MeasureResult(EvaluationMeasure evaluationMeasure, float value) {
        this(evaluationMeasure, value, new Element(evaluationMeasure.getName()), null);
    }

    public MeasureResult(EvaluationMeasure evaluationMeasure, float value, Element element, Object detailedResult) {
        this.element = element;
        this.value = value;
        this.evaluationMeasure = evaluationMeasure;
        this.detailedResult = detailedResult;
    }

    public Element getXMLElement() {
        return element;
    }

    public float getValue() {
        return value;
    }

    public EvaluationMeasure getEvaluationMeasure() {
        return evaluationMeasure;
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
