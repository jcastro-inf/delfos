package delfos.experiment.casestudy.defaultcase;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.RecommendationResults;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 *
 * @version 29-may-2014
 * @author Jorge Castro Gallardo
 */
public class DefaultCaseStudyEvaluationMeasures_Task extends Task {

    protected final int ejecucion;
    protected final int particion;

    protected RatingsDataset<? extends Rating> testSet;
    protected Collection<EvaluationMeasure> evaluationMeasures;
    protected RelevanceCriteria relevanceCriteria;
    protected RecommendationResults recommendationResults;

    /**
     * Campo de salida.
     */
    protected Map<EvaluationMeasure, MeasureResult> executionsResult = new TreeMap<>();

    public DefaultCaseStudyEvaluationMeasures_Task(
            int ejecucion,
            int particion,
            RecommendationResults esr,
            RatingsDataset<? extends Rating> testSet,
            Collection<EvaluationMeasure> evaluationMeasures,
            RelevanceCriteria relevanceCriteria) {
        this.particion = particion;
        this.recommendationResults = esr;
        this.testSet = testSet;
        this.ejecucion = ejecucion;
        this.evaluationMeasures = evaluationMeasures;
        this.relevanceCriteria = relevanceCriteria;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Exec: ").append(ejecucion).append("\tSplit: ").append(particion);

        return stringBuilder.toString();
    }

}
