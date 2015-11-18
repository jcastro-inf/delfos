package delfos.group.casestudy.defaultcase;

import delfos.common.parallelwork.SingleTaskExecute;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;

public class DefaultGroupCaseStudyGroupEvaluationMeasures_SingleTaskExecutor implements SingleTaskExecute<DefaultGroupCaseStudyGroupEvaluationMeasures_Task> {

    public DefaultGroupCaseStudyGroupEvaluationMeasures_SingleTaskExecutor() {
    }

    @Override
    public void executeSingleTask(DefaultGroupCaseStudyGroupEvaluationMeasures_Task task) {
        for (GroupEvaluationMeasure groupEvaluationMeasure : task.groupEvaluationMeasures) {
            GroupEvaluationMeasureResult groupMeasureResult = groupEvaluationMeasure.getMeasureResult(
                    task.groupRecommendationResult,
                    task.testSet,
                    task.relevanceCriteria);
            task.groupEvaluationMeasuresResults.put(groupEvaluationMeasure, groupMeasureResult);
        }
    }
}
