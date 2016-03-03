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
package delfos.group.grs.mean;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRatingRSModel;
import delfos.rs.recommendation.Recommendation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recomendador para grupos de usuarios que recomienda los productos con mejor
 * valoración media. No se recomienda usar este algoritmo en un sistema real
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 9-Junio-2013
 */
public class MeanRatingGRS extends GroupRecommenderSystemAdapter<MeanRatingRSModel, GroupOfUsers> {

    private static final long serialVersionUID = 1L;

    public MeanRatingGRS() {
    }

    @Override
    public MeanRatingRSModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        List<MeanRating> meanRatings
                = datasetLoader.getContentDataset().parallelStream()
                .map(item -> new MeanRatingTask(datasetLoader, item))
                .map(new MeanRatingSingleExecution())
                .sorted(MeanRating.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());

        return new MeanRatingRSModel(meanRatings);
    }

    @Override
    public <RatingType extends Rating> GroupOfUsers buildGroupModel(
            DatasetLoader<RatingType> datasetLoader,
            MeanRatingRSModel RecommendationModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound {
        return new GroupOfUsers(groupOfUsers.getMembers());
    }

    @Override
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, MeanRatingRSModel RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadRatingsDataset {

        Map<Item, MeanRating> meanRatingsByItem = RecommendationModel
                .getSortedMeanRatings().parallelStream()
                .collect(Collectors.toMap(meanRating -> meanRating.getItem(), Function.identity()));

        List<Recommendation> recommendations = candidateItems.parallelStream()
                .map(item -> meanRatingsByItem.containsKey(item) ? meanRatingsByItem.get(item) : new MeanRating(item, Double.NaN))
                .map(meanRating -> new Recommendation(meanRating.getItem(), meanRating.getPreference()))
                .collect(Collectors.toList());

        return new GroupRecommendations(groupOfUsers, recommendations);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }
}
