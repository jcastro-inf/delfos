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

import java.util.Comparator;

public class GroupResultWithExecutionSplit {

    private int execution;
    private int split;
    private GroupEvaluationMeasureResult result;

    public GroupResultWithExecutionSplit(int execution, int split, GroupEvaluationMeasureResult result) {
        this.execution = execution;
        this.split = split;
        this.result = result;
    }

    public static final Comparator<GroupResultWithExecutionSplit> BY_EXECUTION_SPLIT = (result1, result2) -> {
        int executionCompare = Integer.compare(result1.execution, result2.execution);
        if (executionCompare != 0) {
            return executionCompare;
        } else {
            int splitCompare = Integer.compare(result1.split, result2.split);
            return splitCompare;
        }
    };

    public int getExecution() {
        return execution;
    }

    public int getSplit() {
        return split;
    }

    public GroupEvaluationMeasureResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "exec:" + getExecution() + " split:" + getSplit() + " " + getResult().getGroupEvaluationMeasure().getName() + ":" + getResult().getValue();
    }
}
