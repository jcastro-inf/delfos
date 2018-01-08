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
package delfos.results.evaluationmeasures.prspace;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Medida de evaluación que calcula la precisión y recall a lo largo de todos los posibles tamaños de la lista de
 * recomendaciones. Muestra como valor agregado la precisión suponiendo una recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PRSpace extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    public static class UsersWithRecommendationsInTestSet implements Predicate<Integer> {

        private final RecommendationResults recommendationResults;
        private final RatingsDataset<? extends Rating> testDataset;

        public UsersWithRecommendationsInTestSet(
                RecommendationResults recommendationResults,
                RatingsDataset<? extends Rating> testDataset) {
            this.recommendationResults = recommendationResults;
            this.testDataset = testDataset;
        }

        @Override
        public boolean test(Integer idUser) {

            Map<Long, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);
            return recommendationResults
                    .getRecommendationsForUser(idUser).parallelStream()
                    .anyMatch(recommendation -> userRatings.containsKey(recommendation.getItem().getId()));
        }

    }

    List<ConfusionMatricesCurve> curves = new ArrayList<>();

    @Override
    public MeasureResult getMeasureResult(
            RecommendationResults recommendationResults,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria) {

        ConfusionMatricesCurve confusionMatricesCurve = ConfusionMatricesCurve
                .getConfusionMatricesCurve(testDataset, recommendationResults, relevanceCriteria);

        double areaUnderPR = confusionMatricesCurve.getAreaPRSpace();
        curves.add(confusionMatricesCurve);

        return new MeasureResult(
                this,
                areaUnderPR);
    }

}
