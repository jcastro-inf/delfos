package delfos.results.evaluationmeasures.ratingprediction;

import java.util.List;
import java.util.Map;
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
                double trueRating = userRated.get(recommendation.getIdItem()).ratingValue.doubleValue();
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
