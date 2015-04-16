package delfos.group.results.groupevaluationmeasures;

import java.util.Collection;
import org.jdom2.Element;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.MeasureResult;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;

/**
 * Interfaz que define los métodos de una métrica de evaluación de un sistema de
 * recomendación.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (28 Octubre 2012)
 */
/**
 * Clase abstracta que define los métodos que se utilizarán para evaluar un
 * sistema de recomendación a grupos.
 *
* @author Jorge Castro Gallardo
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
     * @param recommendationResults Vector de resultados de la ejecución en el
     * que cada elemento es el resultado de la ejecución con una partición del
     * conjunto
     * @param testDataset
     * @param relevanceCriteria
     * @return Devuelve un objeto GroupMeasureResult que almacena el valor de la
     * métrica para cada ejecución
     */
    public abstract GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria);

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
    public GroupMeasureResult agregateResults(Collection<GroupMeasureResult> results) {
        Element aggregatedElement = new Element(this.getName());
        float aggregatedValue;

        MeanIterative mean = new MeanIterative();
        for (GroupMeasureResult mr : results) {
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

        if (mean.getNumValues() == 0) {
            aggregatedValue = Float.POSITIVE_INFINITY;
            aggregatedElement.setAttribute(EvaluationMeasure.VALUE, Float.toString(Float.POSITIVE_INFINITY));
        } else {
            aggregatedValue = (float) mean.getMean();
            aggregatedElement.setAttribute(EvaluationMeasure.VALUE, Double.toString(mean.getMean()));
        }
        return new GroupMeasureResult(this, aggregatedValue, aggregatedElement);
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

}
