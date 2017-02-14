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
package delfos.results.evaluationmeasures.ndcg;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Evalúa las recomendaciones de un sistema aplicando NDCG, usando logaritmo en base 2. Se calcula el nDCG por usuarios
 * y luego se hace la media.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 18-Noviembre-2013
 */
public class NDCG extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    protected final int listSize;

    public NDCG() {
        this.listSize = -1;
    }

    protected NDCG(int listSize) {
        this.listSize = listSize;
    }

    @Override
    public MeasureResult getMeasureResult(
            RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        List<Double> ndcgPerUser = new ArrayList<>();

        for (int idUser : testDataset.allUsers()) {
            try {

                List<Recommendation> recommendations = recommendationResults.getRecommendationsForUser(idUser);
                if (recommendations.isEmpty()) {
                    continue;
                }

                Map<Integer, Rating> userRatings = (Map<Integer, Rating>) testDataset.getUserRatingsRated(idUser);

                List<Recommendation> idealRecommendations = userRatings
                        .values().parallelStream()
                        .map(rating -> new Recommendation(rating.getItem(), rating.getRatingValue()))
                        .sorted(Recommendation.BY_PREFERENCE_DESC)
                        .collect(Collectors.toList());

                if (listSize > 0) {
                    recommendations = recommendations.stream()
                            .sorted(Recommendation.BY_PREFERENCE_DESC)
                            .limit(listSize)
                            .collect(Collectors.toList());

                    idealRecommendations = idealRecommendations.stream()
                            .limit(listSize)
                            .collect(Collectors.toList());
                }

                double idealGain = computeDCG(idealRecommendations, userRatings, testDataset.getRatingsDomain());
                double gain = computeDCG(recommendations, userRatings, testDataset.getRatingsDomain());
                double score = gain / idealGain;

                if (Double.isNaN(score)) {
                    throw new IllegalStateException("NDCG is NaN, possibly because there are ratings with a Zero value");
                }

                ndcgPerUser.add(score);

            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        return new MeasureResult(this, (double) new MeanIterative(ndcgPerUser).getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    /**
     * Compute the DCG of a list of items with respect to a value vector.
     *
     * @param recommendations
     * @param values
     * @return
     */
    public static double computeDCG(List<Recommendation> recommendations, Map<Integer, ? extends Rating> values, Domain ratingsDomain) {
        final double base = 2;
        final double logBaseChange = Math.log(base);

        double gain = 0;
        int rank = 0;

        for (Recommendation recommendation : recommendations) {
            final int idItem = recommendation.getItem().getId();
            final double rating = values.containsKey(idItem)
                    ? values.get(idItem).getRatingValue().doubleValue()
                    : ratingsDomain.min().doubleValue();
            rank++;

            double discount;

            if (rank < base) {
                discount = 1;
            } else {
                discount = logBaseChange / Math.log(rank);
            }

            double increment = rating * discount;

            gain += increment;
        }

        return gain;
    }

}
