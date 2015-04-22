package delfos.results.evaluationmeasures;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.SingleUserRecommendations;

/**
 * Medida de similitud que calcula la cobertura de una ejecución de un sistema
 * de recomendación. La cobertura representa el ratio de items que se pudo
 * calcular un valor de preferencia del total consultados.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Coverage extends EvaluationMeasure {

    private static final long serialVersionUID = -3387516993124229948L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        float cobertura;

        long total = 0;
        long positivos = 0;

        for (int idUser : testDataset.allUsers()) {
            try {
                Collection<Recommendation> positivosList = recommendationResults.getRecommendationsForUser(idUser);
                Collection<Integer> totalList = testDataset.getUserRated(idUser);
                positivos += positivosList.size();
                total += totalList.size();
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        cobertura = (float) positivos / total;
        return new MeasureResult(this, cobertura);
    }

    @Override
    public MeanIterative getUserResult(SingleUserRecommendations singleUserRecommendations, Map<Integer, ? extends Rating> userRated) {
        MeanIterative userMean = new MeanIterative();
        for (int idItem : userRated.keySet()) {

            Set<Integer> setOfItems = Recommendation.getSetOfItems(singleUserRecommendations.getRecommendations());

            if (setOfItems.contains(idItem)) {
                userMean.addValue(1);
            } else {
                userMean.addValue(0);
            }
        }
        return userMean;
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
