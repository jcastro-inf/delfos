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
package delfos.results.evaluationmeasures.prspace.recall;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.prspace.PRSpace;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class Recall extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    private final int listSize;

    public Recall() {
        listSize = 5;
    }

    public Recall(int listSize) {
        this.listSize = listSize;

    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        Map<Integer, ConfusionMatricesCurve> allUsersCurves = testDataset.allUsers().parallelStream()
                .filter(new PRSpace.UsersWithRecommendationsInTestSet(recommendationResults, testDataset))
                .collect(Collectors.toMap(idUser -> idUser, idUser -> {

                    List<Boolean> resultados = new ArrayList<>(recommendationResults.usersWithRecommendations().size());
                    Collection<Recommendation> recommendations = recommendationResults.getRecommendationsForUser(idUser);
                    Map<Integer, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);

                    recommendations.stream().map(recommendation -> {
                        Item item = recommendation.getItem();
                        if (userRatings.containsKey(item.getId())) {
                            return relevanceCriteria.isRelevant(userRatings.get(item.getId()).getRatingValue());
                        } else {
                            return false;
                        }
                    });

                    return new ConfusionMatricesCurve(resultados);
                }));

        ConfusionMatricesCurve agregada = ConfusionMatricesCurve.mergeCurves(allUsersCurves.values());

        double recallAt;
        if (agregada.size() == 0) {
            recallAt = 0;
        } else {
            int size = Math.min(listSize, agregada.size() - 1);

            recallAt = agregada.getRecallAt(size);
        }

        return new MeasureResult(
                this,
                recallAt);
    }
}
