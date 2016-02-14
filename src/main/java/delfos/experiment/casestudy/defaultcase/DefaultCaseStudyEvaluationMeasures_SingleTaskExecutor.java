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
package delfos.experiment.casestudy.defaultcase;

import delfos.common.Global;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 *
 * @version 29-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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

        Global.showInfoMessage("Finished task " + task.toString() + "\n");
    }

}
