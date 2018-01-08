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
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Medida de evaluación para calcular la raiz del error cuadrático medio normalizado del sistema de recomendación
 * evaluado. Calcula la diferencia entre la valoración hecha para el grupo y la valoración individual que cada usuario
 * dió para el producto, si lo ha valorado.
 *
 * <p>
 * Es una extensión de la medida de evaluación NRMSE para
 * recomendaciones individuales.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class NRMSE extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult, DatasetLoader<? extends Rating> originalDatasetLoader, RelevanceCriteria relevanceCriteria, DatasetLoader<? extends Rating> trainingDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative nrmse = new MeanIterative();

        Domain originalDomain = testDatasetLoader.getRatingsDataset().getRatingsDomain();

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult
                    .getGroupOutput(groupOfUsers)
                    .getRecommendations().getRecommendations();

            Map<Long, Map<Long, ? extends Rating>> groupTrueRatings = new TreeMap<>();
            for (long idUser : groupOfUsers.getIdMembers()) {
                try {
                    groupTrueRatings.put(idUser, testDatasetLoader.getRatingsDataset().getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }

            for (Recommendation recommendation : groupRecommendations) {
                if (Double.isNaN(recommendation.getPreference().doubleValue())) {
                    continue;
                }
                long idItem = recommendation.getIdItem();
                for (long idUser : groupOfUsers.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(idItem)) {
                        double trueRating = groupTrueRatings.get(idUser).get(idItem).getRatingValue().doubleValue();
                        double predictedRating = recommendation.getPreference().doubleValue();

                        double trueRatingNormalised = originalDomain.convertToDecimalDomain(trueRating, DecimalDomain.ZERO_TO_ONE).doubleValue();
                        double predictedNormalised = originalDomain.convertToDecimalDomain(predictedRating, DecimalDomain.ZERO_TO_ONE).doubleValue();

                        nrmse.addValue(Math.pow(predictedNormalised - trueRatingNormalised, 2));
                    }
                }
            }
        }

        if (nrmse.getNumValues() == 0) {
            return new GroupEvaluationMeasureResult(this, Double.NaN);
        } else {
            double rmseValue = (double) Math.sqrt(nrmse.getMean());
            return new GroupEvaluationMeasureResult(this, rmseValue);
        }

    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
