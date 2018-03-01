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
package delfos.rs.bias;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Sistema de recomendación que siempre devuelve una predicción usando el bias
 * general, bias del user y/o bias del item, si existen.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 5-marzo-2015
 */
public class PredictUserItemBias extends CollaborativeRecommender<Object> {

    public PredictUserItemBias() {
        super();
    }

    @Override
    public <RatingType extends Rating> Object buildRecommendationModel(
            DatasetLoader<RatingType> datasetLoader
    ) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        return datasetLoader.getRatingsDataset().getMeanRating();
    }

    @Override
    public <RatingType extends Rating> Collection<Recommendation> recommendToUser(
            DatasetLoader<RatingType> datasetLoader,
            Object model,
            long idUser,
            java.util.Set<Long> candidateItems
    ) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        Collection<Recommendation> recommendations = new ArrayList<>(candidateItems.size());

        RatingsDataset<RatingType> ratingsDataset = datasetLoader.getRatingsDataset();
        double generalBias = ((Number) model).doubleValue();
        double userBias = getUserBias(ratingsDataset, idUser);

        for (long idItem : candidateItems) {
            double itemBias = getItemBias(ratingsDataset, idUser);
            recommendations.add(new Recommendation(idItem, generalBias + userBias + itemBias));
        }

        return recommendations;
    }

    private double getUserBias(RatingsDataset ratingsDataset, Long idUser) throws CannotLoadRatingsDataset {
        double userBias;
        try {
            userBias = ratingsDataset.getMeanRating() - ratingsDataset.getMeanRatingUser(idUser);
        } catch (UserNotFound ex) {
            userBias = 0;
        }

        return userBias;
    }

    private double getItemBias(RatingsDataset ratingsDataset, Long idItem) throws CannotLoadRatingsDataset {
        double itemBias;
        try {
            itemBias = ratingsDataset.getMeanRating() - ratingsDataset.getMeanRatingItem(idItem);
        } catch (ItemNotFound ex) {
            itemBias = 0;
        }

        return itemBias;
    }

}
