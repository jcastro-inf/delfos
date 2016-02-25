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
package delfos.results;

import delfos.results.evaluationmeasures.EvaluationMeasure;
import org.jdom2.Element;

/**
 * Almacena los resultados de una métrica de evaluación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class MeasureResult {

    private final double value;
    private final EvaluationMeasure evaluationMeasure;

    public MeasureResult(EvaluationMeasure evaluationMeasure, double value) {

        this.value = value;
        this.evaluationMeasure = evaluationMeasure;
    }

    public Element getXMLElement() {
        Element measureElement = new Element(evaluationMeasure.getAlias());
        measureElement.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(value));
        return measureElement;
    }

    public double getValue() {
        return value;
    }

    public EvaluationMeasure getEvaluationMeasure() {
        return evaluationMeasure;
    }
}
