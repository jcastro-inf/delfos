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
package delfos.experiment.validation.predictionprotocol;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Esta técnica realiza la composición de recomendaciones de manera que se utilizan todos los ratings del usuario en la
 * predicción excepto el que se desea predecir.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class AllButOne extends PredictionProtocol {

    private static final long serialVersionUID = 1L;

    @Override
    public <RatingType extends Rating> List<Set<Item>> getRecommendationRequests(
            DatasetLoader<RatingType> trainingDatasetLoader,
            DatasetLoader<RatingType> testDatasetLoader,
            User user) throws UserNotFound {
        Collection<RatingType> userRatings= testDatasetLoader.getRatingsDataset().getUserRatingsRated(user.getId()).values();

        List<Set<Item>> collectionOfSetsOfRequests = new LinkedList<>();

        for (RatingType rating : userRatings) {
            Set<Item> oneRequestSet = new TreeSet<>();
            oneRequestSet.add(rating.getItem());
            collectionOfSetsOfRequests.add(oneRequestSet);
        }

        return collectionOfSetsOfRequests;
    }
}
