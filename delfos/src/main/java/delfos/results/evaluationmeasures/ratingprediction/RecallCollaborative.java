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
import java.util.TreeMap;
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
 * Clase que implementa el algoritmo de cálculo del recall en sistemas de
 * recomendación. El recall en predicción se refiere a comprobar que los que el
 * sistema predice como positivos son positivos, es decir, supone que el número
 * de recomendaciones es, para cada usuario, las predicciones que el criterio de
 * relevancia clasifica como positivas.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 (19 de Octubre 2011)
 */
public class RecallCollaborative extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        float recall;
        int relevantesRecomendadas = 0;
        int relevantesNoRecomendadas = 0;
        int noRelevantesRecomendadas = 0;
        int noRelevantesNoRecomendadas = 0;

        for (int idUser : testDataset.allUsers()) {
            try {
                Map<Integer, ? extends Rating> userRatingsRated = testDataset.getUserRatingsRated(idUser);
                TreeMap<Integer, Recommendation> l = new TreeMap<Integer, Recommendation>();
                for (Recommendation r : recommendationResults.getRecommendationsForUser(idUser)) {
                    l.put(r.getIdItem(), r);
                }

                for (int idItem : testDataset.getUserRated(idUser)) {
                    if (l.containsKey(idItem)) {
                        float originalRating = userRatingsRated.get(idItem).getRatingValue().floatValue();
                        float predictedRating = l.get(idItem).getPreference().floatValue();
                        if (relevanceCriteria.isRelevant(originalRating)) {
                            if (relevanceCriteria.isRelevant(predictedRating)) {
                                relevantesRecomendadas++;
                            } else {
                                relevantesNoRecomendadas++;
                            }
                        } else {
                            if (relevanceCriteria.isRelevant(predictedRating)) {
                                noRelevantesRecomendadas++;
                            } else {
                                noRelevantesNoRecomendadas++;
                            }
                        }
                    } else {
                        //Fallo de cobertura
                    }
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        if (relevantesRecomendadas + relevantesNoRecomendadas == 0) {
            recall = 0;
        } else {
            recall = (float) relevantesRecomendadas / ((float) relevantesRecomendadas + (float) relevantesNoRecomendadas);
        }
        return new MeasureResult(this, recall);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
