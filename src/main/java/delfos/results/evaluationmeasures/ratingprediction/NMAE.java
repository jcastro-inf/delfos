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

import delfos.common.Global;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.user.User;
import delfos.dataset.util.DatasetUtilities;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementa NRMSE.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 19-febrero-2014
 */
public class NMAE extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        MeanIterative mean = new MeanIterative();

        Domain originalDomain = testDataset.getRatingsDomain();

        for (int idUser : testDataset.allUsers()) {
            Collection<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            if (recommendationList == null) {
                continue;
            }

            Map<Integer, Double> recommendations = DatasetUtilities
                    .convertToMapOfRecommendations(new RecommendationsToUser(new User(idUser), recommendationList))
                    .entrySet()
                    .parallelStream()
                    .filter(entry -> !Double.isNaN(entry.getValue().getPreference().doubleValue()))
                    .filter(entry -> !Double.isInfinite(entry.getValue().getPreference().doubleValue()))
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().getId(),
                            entry -> entry.getValue().getPreference().doubleValue())
                    );

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

            Set<Integer> commonItems = recommendations.keySet();
            commonItems.retainAll(testRatings.keySet());

            commonItems.stream().forEach(idItem -> {
                Double predictedRating = recommendations.get(idItem);
                Double trueRating = testRatings.get(idItem);

                double trueRatingNormalised = originalDomain.convertToDecimalDomain(trueRating, DecimalDomain.ZERO_TO_ONE).doubleValue();
                double predictedNormalised = originalDomain.convertToDecimalDomain(predictedRating, DecimalDomain.ZERO_TO_ONE).doubleValue();

                mean.addValue(Math.abs(trueRatingNormalised - predictedNormalised));
            });

        }
        if (mean.getNumValues() == 0) {
            Global.showWarning("Cannot compute NMAEsince the RS did not predicted any item with a rating in the test set.");
        }

        return new MeasureResult(this, (double) mean.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
