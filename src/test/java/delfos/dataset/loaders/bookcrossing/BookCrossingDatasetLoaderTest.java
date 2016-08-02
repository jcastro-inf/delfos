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
package delfos.dataset.loaders.bookcrossing;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.rs.collaborativefiltering.Recommender_DatasetProperties;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class BookCrossingDatasetLoaderTest extends DelfosTest {

    public BookCrossingDatasetLoaderTest() {
    }

    /**
     * Test of EPinionsDatasetLoader method, of class BookCrossingDatasetLoader.
     */
    @Test
    public void testEPinionsDatasetLoader() {

        System.out.println("EPinionsDatasetLoader");

        File datasetDirectory = new File("/home/jcastro/Dropbox/Datasets-new/Book-Cross/BX-CSV-Dump");

        BookCrossingDatasetLoader bookCrossingDataset = new BookCrossingDatasetLoader()
                .setDatasetDirectory(datasetDirectory);

        Recommender_DatasetProperties recommender_DatasetProperties = new Recommender_DatasetProperties();

        recommender_DatasetProperties.buildRecommendationModel(bookCrossingDataset);

        Global.showInfoMessage("Showing statistics about the ratings dataset\n");
        Global.showInfoMessage("Num ratings " + bookCrossingDataset.getRatingsDataset().getNumRatings() + "\n");
        Global.showInfoMessage("Num users   " + bookCrossingDataset.getRatingsDataset().allUsers().size() + "\n");
        Global.showInfoMessage("Num items   " + bookCrossingDataset.getRatingsDataset().allRatedItems().size() + "\n");

        Global.showInfoMessage("Showing statistics about the dataset\n");
        Global.showInfoMessage("Num ratings " + bookCrossingDataset.getRatingsDataset().getNumRatings() + "\n");
        Global.showInfoMessage("Num users   " + bookCrossingDataset.getUsersDataset().size() + "\n");
        Global.showInfoMessage("Num items   " + bookCrossingDataset.getContentDataset().size() + "\n");

        int minimumUserRatings = 5;
        int minimumItemRatings = 5;

        RatingsDataset<Rating> ratingsDataset = bookCrossingDataset.getRatingsDataset();

        for (int i = 1; i <= 10; i++) {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("+++++++++++++++++ Iteration " + i + "+++++++++++++++++++++++++++++++++");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            ratingsDataset = filterRatings(ratingsDataset, bookCrossingDataset.getUsersDataset(), bookCrossingDataset.getContentDataset(), minimumUserRatings, minimumItemRatings);
        }

    }

    public RatingsDataset<Rating> filterRatings(RatingsDataset<Rating> ratingsDataset, UsersDataset usersDataset, ContentDataset contentDataset, int minimumUserRatings, int minimumItemRatings) throws CannotLoadContentDataset, CannotLoadUsersDataset {

        List<Rating> ratings = new ArrayList<>(ratingsDataset.getNumRatings());

        Set<User> usersWithRatings = usersDataset.parallelStream()
                .filter(user -> ratingsDataset.getUserRated(user.getId()).size() >= minimumUserRatings)
                .collect(Collectors.toSet());

        Set<Item> itemsWithRatings = contentDataset.parallelStream()
                .filter(item -> ratingsDataset.getItemRated(item.getId()).size() >= minimumItemRatings)
                .collect(Collectors.toSet());

        List<Rating> ratingsFiltered = ratings.parallelStream().filter(rating -> {
            boolean isUserOk = usersWithRatings.contains(rating.getUser());
            boolean isItemOk = itemsWithRatings.contains(rating.getItem());

            return isUserOk && isItemOk;
        })
                .collect(Collectors.toList());

        System.out.println("Num users OK:         " + usersWithRatings.size() + "/" + usersDataset.size());
        System.out.println("Num items OK:         " + itemsWithRatings.size() + "/" + contentDataset.size());
        System.out.println("Num ratings:          " + ratings.size());
        System.out.println("Num ratings filtered: " + ratingsFiltered.size());

        return new BothIndexRatingsDataset<>(ratingsFiltered);
    }
}
