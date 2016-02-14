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
package delfos.io.xml.evaluationmeasures.confusionmatricescurve;

import org.jdom2.Element;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;

/**
 * Genera la información en bruto para representar una curva ROC.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
