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
package delfos.results.evaluationmeasures;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.rs.recommendation.SingleUserRecommendations;
import java.util.Collection;
import java.util.Map;
import org.jdom2.Element;

/**
 * Interfaz que define los métodos de una métrica de evaluación de un sistema de recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 19/10/2011
 * @version 1.1 08-Mar-2013
 */
public abstract class EvaluationMeasure extends ParameterOwnerAdapter implements Comparable<Object> {

    /**
     * Nombre de la característica en que se almacena el valor de la medida de evaluación
     */
    public final static String VALUE_ATTRIBUTE_NAME = "value";

    /**
     * Establece el resultado de una ejecución en base a las recomendaciones hechas y el conjunto de training
     *
     * @param recommendationResults Vector de resultados de la ejecución en el que cada elemento es el resultado de la
     * ejecución con una partición del conjunto
     * @param testDataset Dataset de evaluación que se usan.
     * @param relevanceCriteria Criterio de relevancia que se usa.
     *
     * @return Devuelve un objeto MeasureResult que almacena el valor de la métrica para cada ejecución
     */
    public abstract MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria);

    /**
     * Devuelve true si la interpretación correcta de los valores de la medida supone que el sistema de recomendación
     * asigna las preferencias como una predicción de la valoración que el usuario daría al producto
     *
     * @return true si necesita que el sistema de recomendación a evaluar prediga las valoraciones
     */
    public abstract boolean usesRatingPrediction();

    /**
     * Agrega varios resultados, correspondientes ejecuciones distintas, de esta medida de evaluación. Las clases que
     * hereden de {@link EvaluationMeasure} deben sobreescribir este método si la agregación de la medida de evaluación
     * es compleja.
     *
     * NOTA: Este método agrega los resultados haciendo la media aritmética de los valores devueltos por el método
     * {@link MeasureResult#getValue()}
     *
     * @param results Resultados que se desean agregar
     *
     * @return Devuelve un objeto {@link MeasureResult} que encapsula el resultado agregado de las ejecuciones
     */
    public final MeasureResult agregateResults(Collection<MeasureResult> results) {
        Element aggregatedElement = new Element(this.getName());
        double aggregatedValue;

        MeanIterative mean = new MeanIterative();
        results.stream().forEach((mr) -> {
            mean.addValue(mr.getValue());
        });
        aggregatedValue = (double) mean.getMean();
        aggregatedElement.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(mean.getMean()));

        return new MeasureResult(this, aggregatedValue);
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof EvaluationMeasure) {
            EvaluationMeasure evaluationMeasure = (EvaluationMeasure) o;
            return this.getName().compareTo(evaluationMeasure.getName());
        }
        throw new IllegalStateException("The object compared with is of an unrecognised type.");
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.EVALUATION_MESAURE;
    }

    public Object getUserResult(SingleUserRecommendations singleUserRecommendations, Map<Integer, ? extends Rating> userRated) {
        throw new UnsupportedOperationException();
    }

    public MeasureResult getEvaluationMeasureResultFromXML(Element evaluationMeasureResultElement) {
        String attributeValue = evaluationMeasureResultElement.getAttributeValue(VALUE_ATTRIBUTE_NAME);
        double measureValue = Double.parseDouble(attributeValue);
        return new MeasureResult(this, measureValue);
    }

}
