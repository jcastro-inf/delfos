package delfos.group.casestudy.defaultcase;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class DefaultGroupCaseStudyGroupEvaluationMeasures_Task extends Task {

    //Atributos de entrada.
    int ejecucion;
    int particion;
    RatingsDataset<? extends Rating> testSet;
    GroupRecommendationResult groupRecommendationResult;
    Collection<GroupEvaluationMeasure> groupEvaluationMeasures;
    RelevanceCriteria relevanceCriteria;

    // Atributos para el resultado.
    Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasuresResults;

    public DefaultGroupCaseStudyGroupEvaluationMeasures_Task(
            int ejecucion,
            int particion,
            GroupRecommendationResult groupRecommendationResult,
            RatingsDataset<? extends Rating> testSet,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            RelevanceCriteria relevanceCriteria) {

        this.particion = particion;
        this.groupRecommendationResult = groupRecommendationResult;
        this.testSet = testSet;
        this.ejecucion = ejecucion;

        groupEvaluationMeasuresResults = new TreeMap<>();
        this.groupEvaluationMeasures = groupEvaluationMeasures;
        this.relevanceCriteria = relevanceCriteria;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Exec: ").append(ejecucion).append("\tSplit: ").append(particion);

        return stringBuilder.toString();
    }

    public void clear() {
        this.testSet = null;
        this.groupEvaluationMeasures = null;
    }
}
