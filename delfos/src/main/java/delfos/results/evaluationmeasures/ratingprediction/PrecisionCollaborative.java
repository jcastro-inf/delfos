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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
                    if (relevanceCriteria.isRelevant(userRatingsRated.get(idItem).getRatingValue())) {
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
