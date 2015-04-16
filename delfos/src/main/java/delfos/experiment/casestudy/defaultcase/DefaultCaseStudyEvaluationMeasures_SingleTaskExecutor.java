package delfos.experiment.casestudy.defaultcase;

import delfos.common.Global;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 *
 * @version 29-may-2014
 * @author Jorge Castro Gallardo
 */
public class DefaultCaseStudyEvaluationMeasures_SingleTaskExecutor
        implements SingleTaskExecute<DefaultCaseStudyEvaluationMeasures_Task> {

    @Override
    public void executeSingleTask(DefaultCaseStudyEvaluationMeasures_Task task) {
        try {
            for (EvaluationMeasure e : task.evaluationMeasures) {
                try {
                    MeasureResult measureResult = e.getMeasureResult(task.recommendationResults, task.testSet, task.relevanceCriteria);
                    task.executionsResult.put(e, measureResult);
                } catch (Throwable ex) {
                    Global.showWarning(ex);
                }
            }
        } catch (Throwable ex) {
            Global.showWarning(ex);
        }
        task.recommendationResults.clear();
        task.recommendationResults = null;
        task.evaluationMeasures = null;
        task.testSet = null;

        Global.showMessage("Finished task " + task.toString());
    }

}
