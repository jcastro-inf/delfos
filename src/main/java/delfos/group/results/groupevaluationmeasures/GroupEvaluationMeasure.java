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

import delfos.common.Global;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jdom2.Element;

/**
 * Interfaz que define los métodos de una métrica de evaluación de un sistema de
 * recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (28 Octubre 2012)
 */
/**
 * Clase abstracta que define los métodos que se utilizarán para evaluar un
 * sistema de recomendación a grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class GroupEvaluationMeasure extends ParameterOwnerAdapter implements Comparable<Object> {

    /**
     * Nombre del atributo en que se almacena el valor de la medida de
     * evaluación
     */
    public final static String VALUE = "value";

    /**
     * Establece el resultado de una ejecución en base a las recomendaciones
     * hechas y el conjunto de training
     *
     * @param groupRecommenderSystemResult Vector de resultados de la ejecución
     * en el que cada elemento es el resultado de la ejecución con una partición
     * del conjunto
     * @param originalDatasetLoader
     * @param testDataset
     * @param relevanceCriteria
     * @param trainingDatasetLoader
     * @param testDatasetLoader
     * @return Devuelve un objeto GroupEvaluationMeasureResult que almacena el
     * valor de la métrica para cada ejecución
     */
    public abstract GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader);

    /**
     * Devuelve true si la interpretación correcta de los valores de la medida
     * supone que el sistema de recomendación asigna las preferencias como una
     * predicción de la valoración que el usuario daría al producto
     *
     * @return true si necesita que el sistema de recomendación a evaluar
     * prediga las valoraciones
     */
    public abstract boolean usesRatingPrediction();

    /**
     * Agrega varios resultados, correspondientes ejecuciones distintas, de esta
     * medida de evaluación. Las clases que hereden de {@link EvaluationMeasure}
     * deben sobreescribir este método si la agregación de la medida de
     * evaluación es compleja.
     *
     * NOTA: Este método agrega los resultados haciendo la media aritmética de
     * los valores devueltos por el método {@link MeasureResult#getValue() }
     *
     * @param results Resultados que se desean agregar
     *
     * @return Devuelve un objeto {@link MeasureResult} que encapsula el
     * resultado agregado de las ejecuciones
     */
    public final GroupEvaluationMeasureResult agregateResults(Collection<GroupEvaluationMeasureResult> results) {

        validateCollectionOfResults(results);

        double aggregatedValue;

        MeanIterative mean = new MeanIterative();
        for (GroupEvaluationMeasureResult mr : results) {
            double value = mr.getValue();
            if (Double.isNaN(value)) {
                Global.showWarning("The value for the measure " + this.getName() + " is NaN");
            } else {
                if (Double.isInfinite(value)) {
                    Global.showWarning("The value for the measure " + this.getName() + " is Infinite");
                } else {
                    mean.addValue(mr.getValue());
                }
            }
        }

        aggregatedValue = mean.getNumValues() == 0 ? Double.NaN : mean.getMean();
        return new GroupEvaluationMeasureResult(this, aggregatedValue);
    }

    public void validateCollectionOfResults(Collection<GroupEvaluationMeasureResult> results) {
        Set<GroupEvaluationMeasure> distinctEvaluationMeasures = results.stream()
                .map(result -> result.getGroupEvaluationMeasure())
                .distinct()
                .collect(Collectors.toSet());

        if (distinctEvaluationMeasures.size() != 1) {
            throw new IllegalStateException("Results must belong to the same evaluation measure [" + distinctEvaluationMeasures + "]");
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof GroupEvaluationMeasure) {
            GroupEvaluationMeasure groupEvaluationMeasure = (GroupEvaluationMeasure) o;
            return this.getName().compareTo(groupEvaluationMeasure.getName());
        }

        throw new IllegalStateException("The object compared with is of an unrecognised type.");
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_EVALUATION_MEASURE;
    }

    /**
     * This method returns the extended evaluation measure values. An example of
     * these values are, in case of precision, the precision at different list
     * sizes.
     *
     * @param measureResult Measure result which corresponds with this measure.
     * @return The list of extended performances and their values.
     */
    public Map<String, Number> agregateResultsExtendedPerformance(GroupEvaluationMeasureResult measureResult) {
        return Collections.EMPTY_MAP;
    }

    public GroupEvaluationMeasureResult getGroupEvaluationMeasureResultFromXML(Element groupEvaluationMeasureResultElement) {

        String attributeValue = groupEvaluationMeasureResultElement.getAttributeValue(VALUE);
        double measureValue = Double.parseDouble(attributeValue);
        return new GroupEvaluationMeasureResult(this, measureValue);
    }

}
