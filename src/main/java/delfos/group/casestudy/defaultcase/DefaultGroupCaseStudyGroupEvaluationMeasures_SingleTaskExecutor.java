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
                    task.originalDatasetLoader,
                    task.testSet,
                    task.relevanceCriteria,
                    task.trainingDatasetLoader,
                    task.testDatasetLoader);
            task.groupEvaluationMeasuresResults.put(groupEvaluationMeasure, groupMeasureResult);
        }

        task.clear();
    }
}
