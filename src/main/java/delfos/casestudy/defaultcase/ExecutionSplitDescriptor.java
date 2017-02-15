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
package delfos.casestudy.defaultcase;

import delfos.experiment.casestudy.CaseStudy;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import java.util.Map;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ExecutionSplitDescriptor implements Comparable<ExecutionSplitDescriptor> {

    private final CaseStudy caseStudy;
    private final int execution;
    private final int split;
    private final Map<EvaluationMeasure, MeasureResult> results;

    ExecutionSplitDescriptor(
            int execution,
            int split, CaseStudy caseStudy,
            Map<EvaluationMeasure, MeasureResult> results) {
        this.split = split;
        this.caseStudy = (CaseStudy) caseStudy.clone();
        this.execution = execution;
        this.results = results;
    }

    public int getExecution() {
        return execution;
    }

    public int getSplit() {
        return split;
    }

    public CaseStudy getCaseStudy() {
        return caseStudy;
    }

    public Map<EvaluationMeasure, MeasureResult> getResults() {
        return results;
    }

    @Override
    public int compareTo(ExecutionSplitDescriptor o) {
        int thisExecution = this.getExecution();
        int otherExecution = o.getExecution();

        int executionCompare = Integer.compare(thisExecution, otherExecution);

        if (executionCompare != 0) {
            return executionCompare;
        } else {

            int thisSplit = this.getSplit();
            int otherSplit = o.getSplit();
            int splitCompare = Integer.compare(thisSplit, otherSplit);
            return splitCompare;
        }
    }

}
