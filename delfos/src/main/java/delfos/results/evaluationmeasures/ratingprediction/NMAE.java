package delfos.results.evaluationmeasures.ratingprediction;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;

/**
 * Implementa NRMSE.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
                throw new IllegalStateException("Recommendation list is null.");
            }
            try {
                Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);
                for (Recommendation lista : recommendationList) {
                    Number trueRating = userRated.get(lista.getIdItem()).ratingValue;
                    Number predictedRating = lista.getPreference();

                    if (trueRating != null
                            && !Double.isNaN(trueRating.doubleValue())
                            && !Double.isInfinite(trueRating.doubleValue())
                            && predictedRating != null
                            && !Double.isNaN(predictedRating.doubleValue())
                            && !Double.isInfinite(predictedRating.doubleValue())) {

                        double trueRatingNormalised = originalDomain.convertToDecimalDomain(trueRating, DecimalDomain.ZERO_TO_ONE).doubleValue();
                        double predictedNormalised = originalDomain.convertToDecimalDomain(predictedRating, DecimalDomain.ZERO_TO_ONE).doubleValue();

                        mean.addValue(Math.abs(trueRatingNormalised - predictedNormalised));
                    }
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
    public boolean usesRatingPrediction() {
        return true;
    }
}
