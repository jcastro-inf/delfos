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
import delfos.results.RecommendationResults;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;

/**
 * Compute the root of squared mean absolute error of a collaborative filtering
 * rating prediction
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
            List<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            if (recommendationList != null) {
                try {
                    Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);
                    for (Recommendation lista : recommendationList) {
                        Number trueRating = userRated.get(lista.getIdItem()).ratingValue;
                        Number calculatedRating = lista.getPreference();

                        if (trueRating != null
                                && !Double.isNaN(trueRating.doubleValue())
                                && !Double.isInfinite(trueRating.doubleValue())
                                && calculatedRating != null
                                && !Double.isNaN(calculatedRating.doubleValue())
                                && !Double.isInfinite(calculatedRating.doubleValue())) {
                            mean.addValue(Math.pow(Math.abs(trueRating.floatValue() - calculatedRating.floatValue()), 2));
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

        return new MeasureResult(this, (float) Math.sqrt(mean.getMean()));
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
