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
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class MAE_unpopularItems extends GroupEvaluationMeasure {

    public static final double POPULARITY_THRESHOLD = 0.2;

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult, DatasetLoader<? extends Rating> originalDatasetLoader, RelevanceCriteria relevanceCriteria, DatasetLoader<? extends Rating> trainingDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative maeGeneral = new MeanIterative();
        TreeMap<GroupOfUsers, MeanIterative> maeGroups = new TreeMap<>();
        TreeMap<Integer, MeanIterative> maeAllMembers = new TreeMap<>();

        Map<Item, Double> inverseFrequency = originalDatasetLoader.getContentDataset().parallelStream().collect(Collectors.toMap(item -> item, item -> {

            int numUsersRated = originalDatasetLoader.getRatingsDataset().getItemRated(item.getId()).size();

            double popularity = numUsersRated / (double) originalDatasetLoader.getUsersDataset().size();

            double weight = popularity < POPULARITY_THRESHOLD ? 1 : 0;

            return weight;
        }));

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult
                    .getGroupOutput(groupOfUsers).getRecommendations().getRecommendations();

            if (groupRecommendations.isEmpty()) {
                continue;
            }
            MeanIterative maeGroup = new MeanIterative();
            Map<Integer, MeanIterative> maeMembers = new TreeMap<>();
            for (User member : groupOfUsers.getMembers()) {
                maeMembers.put(member.getId(), new MeanIterative());
            }

            Map<Integer, Map<Integer, ? extends Rating>> groupTrueRatings = new TreeMap<>();

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
                double itemWeight = inverseFrequency.get(item);
                if (itemWeight == 0) {
                    continue;
                }

                for (int idUser : groupOfUsers.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(item.getId())) {
                        double trueRating = groupTrueRatings.get(idUser).get(item.getId()).getRatingValue().doubleValue();
                        double predicted = recommendation.getPreference().doubleValue();
                        double absoluteError = Math.abs(predicted - trueRating);

                        absoluteError = absoluteError * itemWeight;

                        maeGeneral.addValue(absoluteError);
                        maeGroup.addValue(absoluteError);
                        maeMembers.get(idUser).addValue(absoluteError);
                    }
                }
            }

            maeGroups.put(groupOfUsers, maeGroup);
            maeAllMembers.putAll(maeMembers);

        }

        if (maeGeneral.isEmpty()) {
            return new GroupEvaluationMeasureResult(this, Double.NaN);
        } else {
            double mae = maeGeneral.getMean();
            return new GroupEvaluationMeasureResult(this, mae);
        }
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
