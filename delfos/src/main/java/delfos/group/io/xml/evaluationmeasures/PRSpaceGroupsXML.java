package delfos.group.io.xml.evaluationmeasures;

import org.jdom2.Element;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.io.xml.UnrecognizedElementException;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.precisionrecall.PRSpaceGroups;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 22-Jan-2013
 */
public class PRSpaceGroupsXML {

    /**
     * Nombre del elemento de la medida.
     */
    public final static String MEASURE_ELEMENT = PRSpaceGroups.class.getSimpleName();
    /**
     * Nombre del elemento que contienen los puntos de la curva de
     * precisión-recall.
     */
    public final static String DETAILED_RESULT_ELEMENT = "PRCurve";
    /**
     * Nombre del elemento que describe un punto de la curva precision-recall.
     */
    public final static String CURVE_POINT_ELEMENT = "Pair";
    /**
     * Atributo del punto de la curva que tiene el k, es decir, el número de
     * recomendaciones.
     */
    public final static String K_ATTRIBUTE = "k";
    /**
     * Atributo del punto de la curva que tiene la precisión para el número de
     * recomendaciones especificado en el punto.
     */
    public final static String PRECISION_ATTRIBUTE = "precision";
    /**
     * Atributo del punto de la curva que tiene el recall para el número de
     * recomendaciones especificado en el punto.
     */
    public final static String RECALL_ATTRIBUTE = "recall";

    public static Element getElement(ConfusionMatricesCurve curve) {
        Element measureElement = new Element(MEASURE_ELEMENT);
        measureElement.setAttribute(GroupEvaluationMeasure.VALUE,Float.toString(curve.getAreaPRSpace()));
        
        Element detailedResultElement = new Element(DETAILED_RESULT_ELEMENT);

        for (int i = 1; i < curve.size(); i++) {
            
            String precision = Float.toString(curve.getPrecisionAt(i));
            String recall = Float.toString(curve.getRecallAt(i));
            
            Element curvePointElement = new Element(CURVE_POINT_ELEMENT);
            curvePointElement.setAttribute(K_ATTRIBUTE, Integer.toString(i));
            curvePointElement.setAttribute(PRECISION_ATTRIBUTE, precision);
            curvePointElement.setAttribute(RECALL_ATTRIBUTE, recall);
            detailedResultElement.addContent(curvePointElement);
        }
        
        measureElement.addContent(detailedResultElement);

        Element confusionMatricesCurveElement = ConfusionMatricesCurveXML.getElement(curve);
        measureElement.addContent(confusionMatricesCurveElement);
        return measureElement;
    }

    public static ConfusionMatricesCurve getConfusionMatricesCurve(Element e) throws UnrecognizedElementException {
        if (!e.getName().equals(MEASURE_ELEMENT)) {
            throw new UnrecognizedElementException("Element name is '" + e.getName() + "', expected value '" + MEASURE_ELEMENT + "'");
        }
        Element curveElement = e.getChild(ConfusionMatricesCurveXML.CURVE_ELEMENT);
        if (e == null) {
            throw new UnrecognizedElementException(PRSpaceGroupsXML.class.getSimpleName() + ": Curve element not found");
        }
        return ConfusionMatricesCurveXML.getConfusionMatricesCurve(curveElement);

    }
}
