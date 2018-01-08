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
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import static delfos.group.results.groupevaluationmeasures.MAE_unpopular.POPULARITY_THRESHOLD;
import static delfos.group.results.groupevaluationmeasures.MAE_unpopular.getPopularItems;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class MAE_popular extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult, DatasetLoader<? extends Rating> originalDatasetLoader, RelevanceCriteria relevanceCriteria, DatasetLoader<? extends Rating> trainingDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative maePopularItems = new MeanIterative();
        TreeMap<GroupOfUsers, MeanIterative> maeGroups = new TreeMap<>();
        TreeMap<Long, MeanIterative> maeAllMembers = new TreeMap<>();

        Set<Item> popularItems = getPopularItems(originalDatasetLoader, POPULARITY_THRESHOLD);

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult
                    .getGroupOutput(groupOfUsers).getRecommendations().getRecommendations();

            if (groupRecommendations.isEmpty()) {
                continue;
            }
            MeanIterative maeGroup = new MeanIterative();
            Map<Long, MeanIterative> maeMembers = new TreeMap<>();
            for (User member : groupOfUsers.getMembers()) {
                maeMembers.put(member.getId(), new MeanIterative());
            }

            Map<Long, Map<Long, ? extends Rating>> groupTrueRatings = new TreeMap<>();

            groupOfUsers.getIdMembers().stream().forEach((idUser) -> {
                try {
                    groupTrueRatings.put(idUser, testDatasetLoader.getRatingsDataset().getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            });

            for (Recommendation recommendation : groupRecommendations) {
                if (Double.isNaN(recommendation.getPreference().doubleValue())) {
                    continue;
                }
                Item item = recommendation.getItem();

                if (!popularItems.contains(item)) {
                    continue;
                }
                double itemWeight = 1;

                for (long idUser : groupOfUsers.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(item.getId())) {
                        double trueRating = groupTrueRatings.get(idUser).get(item.getId()).getRatingValue().doubleValue();
                        double predicted = recommendation.getPreference().doubleValue();
                        double absoluteError = Math.abs(predicted - trueRating);

                        absoluteError = absoluteError * itemWeight;

                        maePopularItems.addValue(absoluteError);
                        maeGroup.addValue(absoluteError);
                        maeMembers.get(idUser).addValue(absoluteError);
                    }
                }
            }

            maeGroups.put(groupOfUsers, maeGroup);
            maeAllMembers.putAll(maeMembers);

        }

        if (maePopularItems.isEmpty()) {
            return new GroupEvaluationMeasureResult(this, Double.NaN);
        } else {
            double mae = maePopularItems.getMean();
            return new GroupEvaluationMeasureResult(this, mae);
        }
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
