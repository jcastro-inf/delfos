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
package delfos.results.evaluationmeasures.ratingprediction;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatrix;

/**
 * Clase que implementa el algoritmo de cálculo del recall en sistemas de recomendación. El recall en predicción se
 * refiere a comprobar que los que el sistema predice como positivos son positivos, es decir, supone que el número de
 * recomendaciones es, para cada usuario, las predicciones que el criterio de relevancia clasifica como positivas.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (19 de Octubre 2011)
 */
public class RecallCollaborative extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        double recall;

        ConfusionMatrix confusionMatrixCollaborative = PrecisionCollaborative.getConfusionMatrixCollaborative(recommendationResults, testDataset, relevanceCriteria);

        final int truePositive = confusionMatrixCollaborative.getTruePositive();
        final int trueNegative = confusionMatrixCollaborative.getTrueNegative();
        final int falsePositive = confusionMatrixCollaborative.getFalsePositive();
        final int falseNegative = confusionMatrixCollaborative.getFalseNegative();

        if ((truePositive + falseNegative) == 0) {
            recall = 0;
        } else {
            recall = (double) truePositive / (truePositive + falseNegative);
        }

        return new MeasureResult(this, recall);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
