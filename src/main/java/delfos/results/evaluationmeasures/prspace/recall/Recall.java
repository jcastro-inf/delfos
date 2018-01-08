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
package delfos.results.evaluationmeasures.prspace.recall;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class Recall extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    private final int listSize;

    public Recall() {
        listSize = 5;
    }

    public Recall(int listSize) {
        this.listSize = listSize;

    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public MeasureResult getMeasureResult(
            RecommendationResults recommendationResults,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria) {

        ConfusionMatricesCurve confusionMatricesCurve = ConfusionMatricesCurve
                .getConfusionMatricesCurve(
                        testDataset,
                        recommendationResults,
                        relevanceCriteria);

        double recall = confusionMatricesCurve.getRecallAt(listSize);

        return new MeasureResult(
                this,
                recall);
    }
}
