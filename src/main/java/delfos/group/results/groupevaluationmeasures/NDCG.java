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
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import static delfos.results.evaluationmeasures.NDCG.computeDCG;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Evalúa las recomendaciones de un sistema aplicando nDCG, usando logaritmo en base 2. Se calcula el nDCG por usuarios
 * y luego se hace la media. Se aplica la consideración sobre el nDGC hecha por Baltrunas en el paper:
 * <p>
 * <p>
 * Baltrunas L, Makcinskas T, Ricci F (2010) Group recommendations with rank aggregation and collaborative ﬁltering. In:
 * Proceedings of the 4th ACM conference on Recommender Systems, RecSys ’10. ACM, New York, pp 119–126
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 20-Noviembre-2013
 */
public class NDCG extends GroupEvaluationMeasure {

    public NDCG() {
        super();
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult, DatasetLoader<? extends Rating> originalDatasetLoader, RelevanceCriteria relevanceCriteria, DatasetLoader<? extends Rating> trainingDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader) {

        List<Double> ndcgByMember = new ArrayList<>();

        for (GroupOfUsers group : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(group).getRecommendations().getRecommendations();

            if (groupRecommendations.isEmpty()) {
                continue;
            }

            for (int idUser : group) {

                List<Recommendation> idealRecommendations = new ArrayList<>();
                List<Recommendation> recommendationsIntersectUserRatings = new ArrayList<>();
                Map<Integer, ? extends Rating> userRatings;
                try {
                    userRatings = testDatasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    throw new IllegalArgumentException(ex);
                }

                for (Recommendation recommendation : groupRecommendations) {
                    Item item = recommendation.getItem();
                    final Integer idItem = item.getId();

                    if (userRatings.containsKey(idItem)) {
                        idealRecommendations.add(new Recommendation(item, userRatings.get(idItem).getRatingValue()));
                        recommendationsIntersectUserRatings.add(recommendation);
                    }
                }

                if (!idealRecommendations.isEmpty()) {

                    Collections.sort(idealRecommendations);

                    double idealGain = computeDCG(idealRecommendations, userRatings);
                    double gain = computeDCG(recommendationsIntersectUserRatings, userRatings);
                    double score = gain / idealGain;

                    if (Double.isNaN(score)) {
                        throw new IllegalStateException("NDCG is NaN, possibly because there are ratings with a Zero value");
                    }

                    ndcgByMember.add(score);
                }

            }
        }

        double mean = new MeanIterative(ndcgByMember).getMean();

        return new GroupEvaluationMeasureResult(this, mean);
    }

}
