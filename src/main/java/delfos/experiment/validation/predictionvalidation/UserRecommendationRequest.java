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
package delfos.experiment.validation.predictionvalidation;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import java.util.Set;

/**
 * Encapsula los resultados del protocolo de predicción para usuarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RatingType>
 */
public class UserRecommendationRequest<RatingType extends Rating> {

    private final DatasetLoader<RatingType> predictionPhaseDatasetLoader;
    private final User user;
    private final Set<Item> itemsToPredict;

    public UserRecommendationRequest(
            DatasetLoader<RatingType> predictionPhaseDatasetLoader,
            User user,
            Set<Item> itemsToPredict) {

        this.predictionPhaseDatasetLoader = predictionPhaseDatasetLoader;
        this.user = user;
        this.itemsToPredict = itemsToPredict;
    }

    public Set<Item> getItemsToPredict() {
        return itemsToPredict;
    }

    public DatasetLoader<RatingType> getPredictionPhaseDatasetLoader() {
        return predictionPhaseDatasetLoader;
    }

    public User getUser() {
        return user;
    }

}
