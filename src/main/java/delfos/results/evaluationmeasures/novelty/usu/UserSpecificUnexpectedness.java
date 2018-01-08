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
package delfos.results.evaluationmeasures.novelty.usu;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.List;
import java.util.Map;

/**
 * Measures the Global Long-Tail Novelty (see Recommender Systems Handbook, 26.3.3). It is calculated using the
 * probability of items being known by users.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class UserSpecificUnexpectedness extends EvaluationMeasure {

    private final int listSizeOfMeasure;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        double usu = recommendationResults
                .usersWithRecommendations().stream()
                .mapToDouble(idUser -> {

                    return 0.0;
                })
                .average()
                .orElse(Double.NaN);

        return new MeasureResult(this, usu);
    }

    public static double getUserSpecificUnexpectedness(User user, List<Recommendation> recommendations, Map<Item, Rating> userRatings) {
        return Double.NaN;
    }

    public UserSpecificUnexpectedness() {
        this.listSizeOfMeasure = 5;
    }

    protected UserSpecificUnexpectedness(int listSizeOfMeasure) {
        this.listSizeOfMeasure = listSizeOfMeasure;

    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }

}
