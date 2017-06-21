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
package delfos.dataset.storage.validationdatasets;

import java.util.Map;
import java.util.Set;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;

/**
 *
 * @version 14-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ValidationDatasets {

    private static final ValidationDatasets instance = new ValidationDatasets();

    private ValidationDatasets() {

    }

    public static ValidationDatasets getInstance() {
        return instance;
    }

    public <RatingType extends Rating> TrainingRatingsDataset<RatingType> createTrainingDataset(RatingsDataset<RatingType> ratingsDataset, Map<User, Set<Item>> testSet) throws UserNotFound, ItemNotFound {
        return new TrainingRatingsDataset_CPU<>(ratingsDataset, testSet);
    }

    public <RatingType extends Rating> TestRatingsDataset<RatingType> createTestDataset(RatingsDataset<RatingType> ratingsDataset, Map<User, Set<Item>> testSet) throws UserNotFound, ItemNotFound {
        return new TestRatingsDataset_CPU<>(ratingsDataset, testSet);
    }

}
