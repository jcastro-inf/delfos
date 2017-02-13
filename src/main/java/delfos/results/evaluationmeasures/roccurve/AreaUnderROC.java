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
package delfos.results.evaluationmeasures.roccurve;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;

/**
 * Medida de evaluación para calcular el area bajo roc, tomando el tamaño de la lista de recomendaciones como el umbral.
 * Esta medida calcula la sensitividad y especificidad en cada valor del umbral para generar una curva, cuyo área es uno
 * si el clasificador es perfecto y 0,5 si el clasificador es aleatorio.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class AreaUnderROC extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto de la medida de evaluación.
     */
    public AreaUnderROC() {
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

        double areaUnderROC = confusionMatricesCurve.getAreaPRSpace();

        return new MeasureResult(
                this,
                areaUnderROC);
    }
}
