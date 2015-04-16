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
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;

/**
 * Clase que implementa el algoritmo de cálculo de la F-Medida en predicción en
 * sistemas de recomendación colaborativos. La F-Medida en predicción se refiere
 * a comprobar que los que el sistema predice como positivos son positivos, es
 * decir, supone que el número de recomendaciones es, para cada usuario, las
 * predicciones que el criterio de relevancia clasifica como positivas.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 (19 de Octubre 2011)
 */
public class FScoreCollaborative extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para controlar el peso que se asigna a la precisión y al
     * recall. Por defecto vale 1 (igual peso)
     */
    public final static Parameter beta = new Parameter("Beta", new FloatParameter(0.0f, Float.MAX_VALUE, 1.0f));

    /**
     * Crea una instancia de la F-Medida (en inglés <i>F-Score</i> o
     * <i>F-Measure</i>. Por defecto se le asigna beta=1.0
     */
    public FScoreCollaborative() {
        super();
        addParameter(beta);
    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        float precision, recall, fMeasure;
        float beta_ = (Float) getParameterValue(beta);
        int relevantesRecomendadas = 0;
        int relevantesNoRecomendadas = 0;
        int noRelevantesRecomendadas = 0;
        int noRelevantesNoRecomendadas = 0;

        for (int idUser : testDataset.allUsers()) {
            try {
                Map<Integer, ? extends Rating> userRatingsRated = testDataset.getUserRatingsRated(idUser);
                for (Recommendation r : recommendationResults.getRecommendationsForUser(idUser)) {
                    int idItem = r.getIdItem();
                    Rating rating = userRatingsRated.get(idItem);
                    if (rating == null) {
                        Global.showWarning("Error in " + FScoreCollaborative.class.getName() + "\n");
                        continue;
                    }
                    if (relevanceCriteria.isRelevant(rating.ratingValue)) {
                        if (relevanceCriteria.isRelevant(r.getPreference())) {
                            relevantesRecomendadas++;
                        } else {
                            relevantesNoRecomendadas++;
                        }
                    } else {
                        if (relevanceCriteria.isRelevant(r.getPreference())) {
                            noRelevantesRecomendadas++;
                        } else {
                            noRelevantesNoRecomendadas++;
                        }
                    }
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        if (((float) relevantesRecomendadas + (float) noRelevantesRecomendadas) == 0) {
            precision = 0;
        } else {
            precision = (float) relevantesRecomendadas / ((float) relevantesRecomendadas + (float) noRelevantesRecomendadas);
        }

        if (((float) relevantesRecomendadas + (float) relevantesNoRecomendadas) == 0) {
            recall = 0;
        } else {
            recall = (float) relevantesRecomendadas / ((float) relevantesRecomendadas + (float) relevantesNoRecomendadas);
        }

        if ((beta_ * beta_ * precision + recall) == 0) {
            fMeasure = 0;
        } else {
            fMeasure = (1 + beta_ * beta_) * ((precision * recall) / (beta_ * beta_ * precision + recall));
        }
        return new MeasureResult(this, fMeasure);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
