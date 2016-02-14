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

import delfos.io.xml.UnrecognizedElementException;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatrix;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;

/**
 * Clase para efectuar la entrada/salida en xml de curvas ROC.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (10-01-2013)s
 *
 * @see ConfusionMatricesCurve
 */
public class ConfusionMatricesCurveXML {

    /**
     * Constante para indicar el nombre de la característica del par en el que
     * se guarda el k para el que se almacena el punto asociado. El k indica el
     * número de recomendaciones que se hacen al usuario
     */
    public static final String K = "k";
    /**
     * Nombre del elemento en que se guarda un punto de la curva.
     */
    public static final String MATRIX = "Matrix";
    public static final String FALSE_POSITIVE = "falsePositive";
    public static final String FALSE_NEGATIVE = "falseNegative";
    public static final String TRUE_POSITIVE = "truePositive";
    public static final String TRUE_NEGATIVE = "trueNegative";

    public final static String CONFUSION_MATRIX_CURVE_ELEMENT_NAME = ConfusionMatricesCurve.class.getSimpleName();
    public final static String CURVE_ELEMENT = ConfusionMatricesCurve.class.getSimpleName();

    /**
     * El constructor es privado porque esta clase se utiliza mediante sus
     * métodos <code>static</code>.
     */
    private ConfusionMatricesCurveXML() {
    }

    /**
     * Construye un objeto {@link Element} con la información de la curva que
     * recibe como argumento. Este objeto {@link Element} se puede convertir
     * nuevamente a {@link ConfusionMatricesCurve} utilizando el método {@link ConfusionMatricesCurveXML#getConfusionMatricesCurve(org.jdom2.Element)
     * }
     *
     * @param curve Curva que se desea convertir a {@link Element}
     * @return {@link Element} con la información almacenada en la curva
     */
    public static Element getElement(ConfusionMatricesCurve curve) {

        Element curveElement = new Element(CURVE_ELEMENT);

        /*
         * El índice empieza en uno porque así se hace para recuperar los puntos
         * de una ConfusionMatricesCurve
         */
        for (int index = 0; index < curve.size(); index++) {
            Element par = new Element(MATRIX);

            par.setAttribute(K, Integer.toString(index));
            par.setAttribute(FALSE_POSITIVE, Integer.toString(curve.getFalsePositiveAt(index)));
            par.setAttribute(TRUE_POSITIVE, Integer.toString(curve.getTruePositiveAt(index)));
            par.setAttribute(FALSE_NEGATIVE, Integer.toString(curve.getFalseNegativeAt(index)));
            par.setAttribute(TRUE_NEGATIVE, Integer.toString(curve.getTrueNegativeAt(index)));

            curveElement.addContent(par);
        }

        return curveElement;
    }

    /**
     * Convierte un {@link Element} que contenga la información de un
     * ConfusionMatricesCurve en un objeto {@link ConfusionMatricesCurve}.
     *
     *
     * @param element Objeto {@link Element} que contiene la información
     * necesaria para construir el objeto {@link ConfusionMatricesCurve}
     *
     * @return {@link ConfusionMatricesCurve} con la información que el
     * parámetro element contiene
     *
     * @throws UnrecognizedElementException cuando el {@link Element} que se
     * desea convertir no tiene la información que se necesita para construir un
     * {@link ConfusionMatricesCurve} o no está estructurada como se esperaba.
     */
    public static ConfusionMatricesCurve getConfusionMatricesCurve(Element element) throws UnrecognizedElementException {
        List<ConfusionMatrix> matrices = new ArrayList<ConfusionMatrix>();

        /*
         * Recorro todos los elementos que representan un punto dentro de esta
         * curva
         */
        List<Element> pares = element.getChildren(MATRIX);
        if (pares.isEmpty()) {
            if ((element.getChild(CURVE_ELEMENT) != null && !element.getChild(CURVE_ELEMENT).getChildren(MATRIX).isEmpty()) || !element.getChildren().isEmpty()) {
                // (Si el elemento tiene como hijo una curva y este tiene varias matrices )                                 o   el elemento tiene algún hijo.
                //Global.showWarning("BAD USAGE, the curve belongs to a group of users. Perform 'element.getChild(CURVE_ELEMENT)' to call this method");
                pares = element.getChild(CURVE_ELEMENT).getChildren(MATRIX);
            }
        }

        for (Object par : pares) {
            Element parElement = (Element) par;

            int k = Integer.parseInt(parElement.getAttributeValue(K));
            int falsePositive = Integer.parseInt(parElement.getAttributeValue(FALSE_POSITIVE));
            int falseNegative = Integer.parseInt(parElement.getAttributeValue(FALSE_NEGATIVE));
            int truePositive = Integer.parseInt(parElement.getAttributeValue(TRUE_POSITIVE));
            int trueNegative = Integer.parseInt(parElement.getAttributeValue(TRUE_NEGATIVE));
            matrices.add(new ConfusionMatrix(falsePositive, falseNegative, truePositive, trueNegative));
        }

        if (matrices.isEmpty()) {
            return ConfusionMatricesCurve.emptyCurve();
        } else {
            return new ConfusionMatricesCurve(matrices.toArray(new ConfusionMatrix[0]));
        }
    }
}
