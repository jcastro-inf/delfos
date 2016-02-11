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
import delfos.dataset.basic.user.User;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.SingleUserRecommendations;
import java.util.List;
import java.util.Map;

/**
 * Calcula el error absoluto medio de una ejecución con un algoritmo de
 * recomendación que utiliza predicción de valoraciones
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.2 (21-Jan-2013)
 * @version 1.1 (9-Jan-2013)
 * @version 1.0 (Unknow date)
 */
public class MAE extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        MeanIterative mean = new MeanIterative();

        for (int idUser : recommendationResults.usersWithRecommendations()) {
            User user = new User(idUser);
            List<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            try {
                Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);
                MeanIterative userMean = getUserResult(new SingleUserRecommendations(user, recommendationList), userRated);

                if (!userMean.isEmpty()) {
                    mean.addMean(userMean);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }

        }
        if (mean.getNumValues() == 0) {
            Global.showWarning("Cannot compute 'MAE' since the RS did not predicted any recommendation!!");
        }

        return new MeasureResult(this, (float) mean.getMean());
    }

    @Override
    public MeanIterative getUserResult(SingleUserRecommendations singleUserRecommendations, Map<Integer, ? extends Rating> userRated) {
        MeanIterative userMean = new MeanIterative();
        for (Recommendation recommendation : singleUserRecommendations.getRecommendations()) {
            if (userRated.containsKey(recommendation.getIdItem())) {
                double trueRating = userRated.get(recommendation.getIdItem()).getRatingValue().doubleValue();
                double calculatedRating = recommendation.getPreference().doubleValue();
                userMean.addValue(Math.abs(trueRating - calculatedRating));
            }
        }
        return userMean;
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
