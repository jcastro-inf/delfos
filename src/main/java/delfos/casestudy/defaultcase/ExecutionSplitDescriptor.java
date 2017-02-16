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

import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.CaseStudy;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import java.util.Map;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RecommendationModel>
 * @param <RatingType>
 */
public class ExecutionSplitDescriptor<RecommendationModel extends Object, RatingType extends Rating> implements Comparable<ExecutionSplitDescriptor<RecommendationModel, RatingType>> {

    private final CaseStudy<RecommendationModel, RatingType> caseStudy;
    private final int execution;
    private final int split;
    private final Map<EvaluationMeasure, MeasureResult> results;

    public ExecutionSplitDescriptor(
            int execution,
            int split, CaseStudy<RecommendationModel, RatingType> caseStudy,
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

    public CaseStudy<RecommendationModel, RatingType> getCaseStudy() {
        return caseStudy;
    }

    public Map<EvaluationMeasure, MeasureResult> getResults() {
        return results;
    }

    @Override
    public int compareTo(ExecutionSplitDescriptor<RecommendationModel, RatingType> o) {
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
