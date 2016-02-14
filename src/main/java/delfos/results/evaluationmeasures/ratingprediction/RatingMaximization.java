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
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.List;

/**
 * Calcula la suma de ratings recomendados a cada usuario y hace la media. El
 * valor devuelto es entre 0 y 1, siendo 1 el mejor valor que indica que todas
 * las recomendaciones fueron valoradas con la máxima puntuación en el conjunto
 * de test.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 UnkowDate
 */
public class RatingMaximization extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        if (true) {
            throw new UnsupportedOperationException("No está correctamente implementado, debe dar un rating maximization por cada tamaño de la lista de reocmendación");
        }
        double value = 0;
        int numRecommendations = 0;
        for (int idUser : testDataset.allUsers()) {

            List<Recommendation> thisUserRecommendations = recommendationResults.getRecommendationsForUser(idUser);
            for (int i = 0; i < thisUserRecommendations.size(); i++) {
                int idItem = thisUserRecommendations.get(i).getIdItem();
                try {
                    value += testDataset.getRating(idUser, idItem).getRatingValue().doubleValue();
                    numRecommendations++;
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                } catch (ItemNotFound ex) {
                    ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                }

            }
        }
        if (numRecommendations == 0) {
            Global.showWarning("Cannot compute 'RatingMaximization' since the RS did not predicted any recommendation!!");
            value = 0;
        } else {
            double maxValue = numRecommendations * testDataset.getRatingsDomain().max().doubleValue();
            double minValue = numRecommendations * testDataset.getRatingsDomain().min().doubleValue();
            value = (value - minValue) / (maxValue - minValue);
        }
        return new MeasureResult(this, (float) value);
    }
}
