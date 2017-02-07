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
package delfos.results.evaluationmeasures.prediction.list;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.List;
import java.util.Map;

/**
 * Medida de evaluación para calcular la probabilidad de que un usuario vea un ítem bueno en una lista de tamaño alfa,
 * considerando que la probabilidad decrece a medida que el usuario está más abajo en la lista. Alfa es la mitad de la
 * vida de la lista, es decir, el número de items que el usuario ve a una probabilidad del 50%. (Breese et. al 1998).
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1-julio-2014
 */
public class HalfLifeUtility extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    public static final Parameter ALPHA = new Parameter("ALPHA", new IntegerParameter(1, 500, 5));
    public static final Parameter NEUTRAL_RATING = new Parameter("NEUTRAL_RATING", new DoubleParameter(-500, 500, 3));
    private Integer oldAlpha = 5;

    /**
     * Constructor por defecto de la medida de evaluación.
     */
    public HalfLifeUtility() {
        super();

        addParameter(ALPHA);
        addParameter(NEUTRAL_RATING);

        addParammeterListener(() -> {
            int newAlpha = (Integer) getParameterValue(ALPHA);

            if (oldAlpha != newAlpha) {
                String newAlias = HalfLifeUtility.this.getClass().getSimpleName() + "_at_" + newAlpha;
                setAlias(newAlias);
                oldAlpha = newAlpha;
            }
        });
    }

    public HalfLifeUtility(int alpha) {
        this();

        setParameterValue(ALPHA, alpha);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        MeanIterative mean = new MeanIterative();

        final double alpha = ((Number) getParameterValue(ALPHA)).doubleValue();
        final double neutralRating = ((Number) getParameterValue(NEUTRAL_RATING)).doubleValue();

        for (int idUser : testDataset.allUsers()) {

            List<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);

            if (recommendationList.isEmpty()) {
                continue;
            }
            double sum = 0;

            try {
                Map<Integer, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);

                int j = 1;

                for (Recommendation recommendation : recommendationList) {
                    int idItem = recommendation.getItem().getId();

                    double prediction = recommendation.getPreference().doubleValue();
                    double rating = userRatings.containsKey(idItem)
                            ? userRatings.get(idItem).getRatingValue().doubleValue()
                            : neutralRating;

                    double numerator = rating - neutralRating;
                    double denominator = Math.pow(2, (j - 1) / (alpha - 1));

                    double thisLoopSum = Math.max(0, numerator) / denominator;

                    sum += thisLoopSum;

                    j++;
                }

                mean.addValue(sum);
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }

        }

        return new MeasureResult(this, (double) mean.getMean());
    }
}
