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

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;

/**
 * Compute the root of squared mean absolute error of a collaborative filtering
 * rating prediction
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 UnkowDate
 */
public class RMSE extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        MeanIterative mean = new MeanIterative();

        for (int idUser : testDataset.allUsers()) {
            Collection<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            if (recommendationList != null) {
                try {
                    Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);
                    for (Recommendation lista : recommendationList) {
                        Number trueRating = userRated.get(lista.getIdItem()).getRatingValue();
                        Number calculatedRating = lista.getPreference();

                        if (trueRating != null
                                && !Double.isNaN(trueRating.doubleValue())
                                && !Double.isInfinite(trueRating.doubleValue())
                                && calculatedRating != null
                                && !Double.isNaN(calculatedRating.doubleValue())
                                && !Double.isInfinite(calculatedRating.doubleValue())) {
                            mean.addValue(Math.pow(Math.abs(trueRating.doubleValue() - calculatedRating.doubleValue()), 2));
                        }
                    }
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }

        }
        if (mean.getNumValues() == 0) {
            Global.showWarning("Cannot compute 'MAE' since the RS did not predicted any recommendation!!");
        }

        return new MeasureResult(this, (double) Math.sqrt(mean.getMean()));
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
