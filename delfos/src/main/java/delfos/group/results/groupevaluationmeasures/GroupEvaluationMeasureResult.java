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
package delfos.group.results.groupevaluationmeasures;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 * Almacena los resultados de una métrica de evaluación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (28 Octubre 2012)
 */
public class GroupEvaluationMeasureResult {

    private final Element element;
    private final double value;
    private final GroupEvaluationMeasure groupEvaluationMeasure;
    private final Object detailedResult;

    public GroupEvaluationMeasureResult(GroupEvaluationMeasure groupEvaluationMeasure, double value, Element element, Object detailedResult) {
        this.element = element;
        this.value = value;
        this.groupEvaluationMeasure = groupEvaluationMeasure;
        this.detailedResult = detailedResult;
    }

    public GroupEvaluationMeasureResult(GroupEvaluationMeasure groupEvaluationMeasure, double value, Element element) {
        this(groupEvaluationMeasure, value, element, null);
    }

    public GroupEvaluationMeasureResult(GroupEvaluationMeasure groupEvaluationMeasure, double value) {
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
