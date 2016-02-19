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
package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Medida de evaluación para calcular el error cuadrático medio del sistema de
 * recomendación evaluado. Calcula la diferencia entre la valoración hecha para
 * el grupo y la valoración individual que cada usuario dió para el producto, si
 * lo ha valorado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.RatingPrediction.RMSE} para
 * recomendaciones individuales.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (22-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE_ForGroups
 */
public class RMSE extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative rmse = new MeanIterative();

        for (GroupOfUsers group : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(group).getRecommendations();

            Map<Integer, Map<Integer, ? extends Rating>> groupTrueRatings = new TreeMap<>();
            for (int idUser : group.getIdMembers()) {
                try {
                    groupTrueRatings.put(idUser, testDataset.getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }

            for (Recommendation recommendation : groupRecommendations) {
                if (Double.isNaN(recommendation.getPreference().doubleValue())) {
                    continue;
                }
                int idItem = recommendation.getIdItem();
                for (int idUser : group.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(idItem)) {
                        double trueRating = groupTrueRatings.get(idUser).get(idItem).getRatingValue().doubleValue();
                        double predicted = recommendation.getPreference().doubleValue();
                        rmse.addValue(Math.pow(predicted - trueRating, 2));
                    }
                }
            }
        }

        if (rmse.getNumValues() == 0) {
            return new GroupEvaluationMeasureResult(this, Double.NaN);
        } else {
            float rmseValue = (float) Math.sqrt(rmse.getMean());
            return new GroupEvaluationMeasureResult(this, rmseValue);
        }

    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
