package delfos.results.evaluationmeasures.ratingprediction;

import java.util.Map;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.ERROR_CODES;
import delfos.rs.recommendation.Recommendation;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.RecommendationResults;
import delfos.results.MeasureResult;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Clase que implementa el algoritmo de cálculo de la precisión en predicción en
 * sistemas de recomendación colaborativos. La precisión en predicción se
 * refiere a comprobar que los que el sistema predice como positivos son
 * positivos, es decir, supone que el número de recomendaciones es, para cada
 * usuario, las predicciones que el criterio de relevancia clasifica como
 * positivas.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 (19 de Octubre 2011)
 */
public class PrecisionCollaborative extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        float precision;
        int relevantesRecomendadas = 0;
        int relevantesNoRecomendadas = 0;
        int noRelevantesRecomendadas = 0;
        int noRelevantesNoRecomendadas = 0;

        for (int idUser : testDataset.allUsers()) {
            try {
                Map<Integer, ? extends Rating> userRatingsRated = testDataset.getUserRatingsRated(idUser);
                for (Recommendation r : recommendationResults.getRecommendationsForUser(idUser)) {
                    int idItem = r.getIdItem();
                    if (relevanceCriteria.isRelevant(userRatingsRated.get(idItem).ratingValue)) {
                        if (relevanceCriteria.isRelevant(r.getPreference())) {
                            relevantesRecomendadas++;
                        } else {
                            relevantesNoRecomendadas++;
                        }
                    } else if (relevanceCriteria.isRelevant(r.getPreference())) {
                        noRelevantesRecomendadas++;
                    } else {
                        noRelevantesNoRecomendadas++;
                    }
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        if ((relevantesRecomendadas + noRelevantesRecomendadas) == 0) {
            precision = 0;
        } else {
            precision = (float) relevantesRecomendadas / ((float) relevantesRecomendadas + (float) noRelevantesRecomendadas);
        }

        return new MeasureResult(this, precision);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
