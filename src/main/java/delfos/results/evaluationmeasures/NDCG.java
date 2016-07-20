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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        List<Double> ndcgPerUser = new ArrayList<>();

        for (int idUser : testDataset.allUsers()) {
            try {

                List<Recommendation> recommendations = recommendationResults.getRecommendationsForUser(idUser);
                if (recommendations.isEmpty()) {
                    continue;
                }

                List<Recommendation> idealRecommendations = new ArrayList<>(recommendations.size());
                Map<Integer, Rating> userRatings = (Map<Integer, Rating>) testDataset.getUserRatingsRated(idUser);

                for (Recommendation recommendation : recommendations) {
                    int idItem = recommendation.getIdItem();
                    idealRecommendations.add(new Recommendation(idItem, userRatings.get(idItem).getRatingValue()));
                }

                double idealGain = computeDCG(idealRecommendations, userRatings);
                double gain = computeDCG(recommendations, userRatings);
                double score = gain / idealGain;
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
     * @param items
     * @param values
     * @return
     */
    public static double computeDCG(List<Recommendation> items, Map<Integer, ? extends Rating> values) {
        final double base = 2;
        final double logBaseChange = Math.log(base);

        double gain = 0;
        int rank = 0;

        Iterator<Recommendation> iit = items.iterator();
        while (iit.hasNext()) {
            final int idItem = iit.next().getItem().getId();
            final double rating = values.get(idItem).getRatingValue().doubleValue();
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
