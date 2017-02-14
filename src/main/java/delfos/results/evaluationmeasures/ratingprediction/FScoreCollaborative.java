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
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

        final AtomicInteger falsePositive = new AtomicInteger(0);
        final AtomicInteger truePositive = new AtomicInteger(0);
        final AtomicInteger falseNegative = new AtomicInteger(0);
        final AtomicInteger trueNegative = new AtomicInteger(0);

        for (int idUser : testDataset.allUsers()) {
            Collection<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            if (recommendationList == null) {
                continue;
            }

            Map<Integer, Double> testRatings = testDataset
                    .getUserRatingsRated(idUser)
                    .values()
                    .parallelStream()
                    .filter(rating -> !Double.isNaN(rating.getRatingValue().doubleValue()))
                    .filter(rating -> !Double.isInfinite(rating.getRatingValue().doubleValue()))
                    .collect(Collectors.toMap(
                            rating -> rating.getItem().getId(),
                            rating -> rating.getRatingValue().doubleValue())
                    );

            recommendationList.parallelStream().forEach(recommendation -> {
                final Integer idItem = recommendation.getItem().getId();

                if (relevanceCriteria.isRelevant(recommendation.getPreference())) {

                    //positive
                    if (!testRatings.containsKey(idItem) || !relevanceCriteria.isRelevant(testRatings.get(idItem))) {
                        falsePositive.incrementAndGet();
                    } else {
                        truePositive.incrementAndGet();
                    }
                } else if (!testRatings.containsKey(idItem) || !relevanceCriteria.isRelevant(testRatings.get(idItem))) {
                    trueNegative.incrementAndGet();
                } else {
                    falsePositive.incrementAndGet();
                }

            });

        }

        if ((truePositive.get() + falsePositive.get()) == 0) {
            precision = 0;
        } else {
            precision = truePositive.get() / (truePositive.get() + falsePositive.get());
        }

        if ((truePositive.get() + falseNegative.get()) == 0) {
            recall = 0;
        } else {
            recall = (double) truePositive.get() / ((double) truePositive.get() + (double) falseNegative.get());
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
