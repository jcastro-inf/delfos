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
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Iterator;
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

        for (long idUser : testDataset.allUsers()) {
            try {

                List<Recommendation> recommendations = recommendationResults.getRecommendationsForUser(idUser);
                if (recommendations.isEmpty()) {
                    continue;
                }

                Map<Long, Rating> userRatings = (Map<Long, Rating>) testDataset.getUserRatingsRated(idUser);


                double ndcg = computeNDCG(recommendations, userRatings);

                ndcgPerUser.add(ndcg);

            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        final double ndcg = (double) new MeanIterative(ndcgPerUser).getMean();

        return new MeasureResult(this, ndcg);
    }

    public static <RatingType extends Rating >  double computeNDCG(List<Recommendation> recommendations, Map<Long, RatingType> userRatings){
        return computeNDCG(recommendations,userRatings,-1);
    }

    public static <RatingType extends Rating >  double computeNDCG(List<Recommendation> recommendations, Map<Long, RatingType> userRatings, int listSize) {

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
                    .sorted(Recommendation.BY_PREFERENCE_DESC)
                    .limit(listSize)
                    .collect(Collectors.toList());
        }


        double idealGain = computeDCG(idealRecommendations, userRatings);
        double gain = computeDCG(recommendations, userRatings);

        double score = gain / idealGain;

        if (Double.isNaN(score)) {
            throw new IllegalStateException("NDCG is NaN, possibly because there are ratings with a Zero value");
        }

        return score;
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    /**
     * Compute the DCG of a list of items with respect to a value vector.
     *
     * @param recommendations
     * @param userRatings
     * @return
     */
    public static <RatingType extends Rating >  double computeDCG(
            List<Recommendation> recommendations,
            Map<Long, RatingType> userRatings) {

        double gain = 0;
        int rank = 0;

        Iterator<Recommendation> iit = recommendations.iterator();
        while (iit.hasNext()) {
            final Recommendation recommendation = iit.next();
            final long idItem = recommendation.getItem().getId();

            if (!userRatings.containsKey(idItem)) {
                continue;
            }

            final double rating = userRatings.get(idItem).getRatingValue().doubleValue();
            rank++;

            double discount;

            if (rank < base) {
                discount = 1;
            } else {
                discount = 1 / log2(rank);
            }

            double increment = rating * discount;

            gain += increment;
        }

        return gain;
    }

    private static final int base = 2;
    private static final double logBaseChange = Math.log(base);

    public static double log2(int rank) {
        double log2OfRank = Math.log(rank) / logBaseChange;
        return log2OfRank;
    }
}
