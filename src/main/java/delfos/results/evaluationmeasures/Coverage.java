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
package delfos.results.evaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Medida de similitud que calcula la cobertura de una ejecución de un sistema de recomendación. La cobertura representa
 * el ratio de items que se pudo calcular un valor de preferencia del total consultados.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Coverage extends EvaluationMeasure {

    private static final long serialVersionUID = -3387516993124229948L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        AtomicLong requested = new AtomicLong(0);
        AtomicLong predicted = new AtomicLong(0);

        testDataset.allUsers().parallelStream().forEach(idUser -> {
            try {
                final List<Recommendation> recommendationsForUser = recommendationResults
                        .getRecommendationsForUser(idUser);

                Collection<Recommendation> recommendationsNonCoverageFailures = recommendationsForUser
                        .parallelStream()
                        .filter(Recommendation.NON_COVERAGE_FAILURES)
                        .collect(Collectors.toList());

                Collection<Integer> itemsRatedByUser = testDataset.getUserRated(idUser);
                predicted.addAndGet(recommendationsNonCoverageFailures.size());
                requested.addAndGet(itemsRatedByUser.size());
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }

        });

        final double coverage = ((double) predicted.get()) / ((double) requested.get());
        return new MeasureResult(this, coverage);
    }

    @Override
    public MeasureResult getUserResult(
            RecommendationsToUser recommendationsToUser,
            Map<Integer, ? extends Rating> userRated) {

        MeanIterative userMean = new MeanIterative();
        userRated.keySet().stream().forEach((idItem) -> {
            Set<Integer> setOfItems = Recommendation
                    .getSetOfItems(recommendationsToUser.getRecommendations());

            if (setOfItems.contains(idItem)) {
                userMean.addValue(1);
            } else {
                userMean.addValue(0);
            }
        });
        return new MeasureResult(this, userMean.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
