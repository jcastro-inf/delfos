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

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatrix;

/**
 * Clase que implementa el algoritmo de cálculo de la F-Medida en predicción en sistemas de recomendación colaborativos.
 * La F-Medida en predicción se refiere a comprobar que los que el sistema predice como positivos son positivos, es
 * decir, supone que el número de recomendaciones es, para cada usuario, las predicciones que el criterio de relevancia
 * clasifica como positivas.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (19 de Octubre 2011)
 */
public class FScoreCollaborative extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para controlar el peso que se asigna a la precisión y al recall. Por defecto vale 1 (igual peso)
     */
    public final static Parameter BETA = new Parameter("Beta", new DoubleParameter(0.0f, Double.MAX_VALUE, 1.0f));

    /**
     * Crea una instancia de la F-Medida (en inglés <i>F-Score</i> o
     * <i>F-Measure</i>. Por defecto se le asigna BETA=1.0
     */
    public FScoreCollaborative() {
        super();
        addParameter(BETA);
    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        double precision, recall, fMeasure;
        double beta_ = (Double) getParameterValue(BETA);

        ConfusionMatrix confusionMatrixCollaborative = PrecisionCollaborative.getConfusionMatrixCollaborative(recommendationResults, testDataset, relevanceCriteria);

        final int truePositive = confusionMatrixCollaborative.getTruePositive();
        final int trueNegative = confusionMatrixCollaborative.getTrueNegative();
        final int falsePositive = confusionMatrixCollaborative.getFalsePositive();
        final int falseNegative = confusionMatrixCollaborative.getFalseNegative();

        if ((truePositive + falsePositive) == 0) {
            precision = 0;
        } else {
            precision = (double) truePositive / (truePositive + falsePositive);
        }

        if ((truePositive + falseNegative) == 0) {
            recall = 0;
        } else {
            recall = (double) truePositive / (truePositive + falseNegative);
        }

        if ((beta_ * beta_ * precision + recall) == 0) {
            fMeasure = 0;
        } else {
            fMeasure = (1 + beta_ * beta_) * ((precision * recall) / (beta_ * beta_ * precision + recall));
        }
        return new MeasureResult(this, fMeasure);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
