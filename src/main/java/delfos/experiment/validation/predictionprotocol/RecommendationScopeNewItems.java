/*
 * Copyright (C) 2017 jcastro
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
package delfos.experiment.validation.predictionprotocol;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This prediction protodol requests all the items that a real recommender system would recommend to a user considering
 * that already rated items are not recommended. This validation is useful to study coverage, diversity and novelty
 * simulating a real recommendation scenario.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class RecommendationScopeNewItems extends PredictionProtocol {

    public static final long serialVersionUID = 1L;

    @Override
    public <RatingType extends Rating> List<Set<Item>> getRecommendationRequests(
            DatasetLoader<RatingType> trainingDatasetLoader,
            DatasetLoader<RatingType> testDatasetLoader,
            User user)
            throws UserNotFound {

        Set<Item> userRatedItems = trainingDatasetLoader.getRatingsDataset().getUserRatingsRated(user.getId())
                .values().stream().map(rating-> rating.getItem()).collect(Collectors.toSet());

        Set<Item> itemsNotRated = trainingDatasetLoader.getContentDataset()
                .parallelStream()
                .filter(item -> !userRatedItems.contains(item))
                .collect(Collectors.toSet());

        List<Set<Item>> recommendationRequests = new ArrayList<>();
        recommendationRequests.add(itemsNotRated);

        return recommendationRequests;
    }

    @Override
    public <RatingType extends Rating> List<Set<Item>> getRatingsToHide(
            DatasetLoader<RatingType> trainingDatasetLoader,
            DatasetLoader<RatingType> testDatasetLoader,
            User user)
            throws UserNotFound {

        List<Set<Item>> ratingsToHide = new ArrayList<>();
        ratingsToHide.add(Collections.EMPTY_SET);
        return ratingsToHide;

    }

}
