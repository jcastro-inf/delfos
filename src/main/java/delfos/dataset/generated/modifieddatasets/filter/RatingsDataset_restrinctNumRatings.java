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
package delfos.dataset.generated.modifieddatasets.filter;

import delfos.common.Global;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.utils.streams.IteratorToList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro
 * @param <RatingType>
 */
public class RatingsDataset_restrinctNumRatings<RatingType extends Rating> extends BothIndexRatingsDataset<RatingType> {

    public static <RatingType extends Rating> RatingsDataset<RatingType> buildFilteringRatingsUntilAllConditionsAreSatisfied(
            DatasetLoader<RatingType> datasetLoader,
            int minimumUserRatings,
            int minimumItemRatings) {
        DatasetLoader<RatingType> newDatasetLoader = new DatasetLoaderGivenRatingsDataset<>(datasetLoader, datasetLoader.getRatingsDataset());

        int i = 1;
        while (true) {
            if (Global.isInfoPrinted()) {
                Global.showInfoMessage("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
                Global.showInfoMessage("+++++++++++++++++ Iteration " + i + " ++++++++++++++++++++++++++++++++\n");
                Global.showInfoMessage("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
            }

            int oldNumRatings = newDatasetLoader.getRatingsDataset().getNumRatings();

            RatingsDataset_restrinctNumRatings<RatingType> newRatingsDataset = new RatingsDataset_restrinctNumRatings<>(newDatasetLoader, minimumUserRatings, minimumItemRatings);
            newDatasetLoader = new DatasetLoaderGivenRatingsDataset<>(newDatasetLoader, newRatingsDataset);

            int newNumRatings = newDatasetLoader.getRatingsDataset().getNumRatings();
            if (oldNumRatings == newNumRatings) {
                break;
            }
            i++;
        }

        return newDatasetLoader.getRatingsDataset();
    }

    public static <RatingType extends Rating> RatingsDataset<? extends RatingType> buildFilteringRatings(DatasetLoader<? extends RatingType> datasetLoader,
            int minimumUserRatings,
            int minimumItemRatings) {

        return new RatingsDataset_restrinctNumRatings<>(datasetLoader, minimumUserRatings, minimumItemRatings);
    }

    public static <RatingType extends Rating> RatingsDataset<? extends RatingType> filterRatings(
            DatasetLoader<RatingType> datasetLoader,
            int minimumUserRatings,
            int minimumItemRatings) {

        RatingsDataset<RatingType> ratingsDataset = datasetLoader.getRatingsDataset();

        List<RatingType> ratings = IteratorToList.collectInList(ratingsDataset);

        Set<User> usersWithRatings = datasetLoader.getUsersDataset().parallelStream()
                .filter(user -> ratingsDataset.getUserRated(user.getId()).size() >= minimumUserRatings)
                .collect(Collectors.toSet());

        Set<Item> itemsWithRatings = datasetLoader.getContentDataset().parallelStream()
                .filter(item -> ratingsDataset.getItemRated(item.getId()).size() >= minimumItemRatings)
                .collect(Collectors.toSet());

        List<RatingType> ratingsFiltered = ratings.parallelStream().filter(rating -> {
            boolean isUserOk = usersWithRatings.contains(rating.getUser());
            boolean isItemOk = itemsWithRatings.contains(rating.getItem());

            return isUserOk && isItemOk;
        })
                .collect(Collectors.toList());

        if (Global.isInfoPrinted()) {
            Global.showInfoMessage("Num users OK:         " + usersWithRatings.size() + "/" + datasetLoader.getUsersDataset().size() + "\n");
            Global.showInfoMessage("Num items OK:         " + itemsWithRatings.size() + "/" + datasetLoader.getContentDataset().size() + "\n");
            Global.showInfoMessage("Num ratings:          " + ratings.size() + "\n");
            Global.showInfoMessage("Num ratings filtered: " + ratingsFiltered.size() + "\n");
        }

        return new BothIndexRatingsDataset<>(ratingsFiltered);
    }

    public RatingsDataset_restrinctNumRatings(DatasetLoader<RatingType> datasetLoader, int minUserRatings, int minItemRatings) {
        super(
                filterRatings(
                        datasetLoader,
                        minUserRatings,
                        minItemRatings
                )
        );
    }

}
