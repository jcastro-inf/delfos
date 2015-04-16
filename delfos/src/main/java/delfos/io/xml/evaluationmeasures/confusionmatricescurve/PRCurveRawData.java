package delfos.io.xml.evaluationmeasures.confusionmatricescurve;

import org.jdom2.Element;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;

/**
 * Genera la información en bruto para representar una curva ROC.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (11-01-2013)
 */
public class PRCurveRawData implements CurveRawDataGenerator {

    /**
     * Nombre del tipo de información en bruto que se genera.
     */
    public static final String TYPE_NAME = "Precision-recall_curve";

    @Override
    public Element getRawDataElement(ConfusionMatricesCurve curve) {
        Element ret = new Element(RAW_DATA_ELEMENT);
        ret.setAttribute(RAW_DATA_TYPE_ATTRIBUTE, TYPE_NAME);
        
        StringBuilder b = new StringBuilder();
        b.append("\n");
        for(int index=0;index<curve.size();index++){
            b.append(curve.getPrecisionAt(index));
            b.append("\t");
            b.append(curve.getRecallAt(index));
            b.append("\n");
        }
        b.append("\n");
        ret.addContent(b.toString());
        return ret;
    }
}
