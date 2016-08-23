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
package delfos.dataset.loaders.movilens.ml1m;

import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.modifieddatasets.filter.RatingsDataset_restrinctNumRatings;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.rs.collaborativefiltering.Recommender_DatasetProperties;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class MovieLens1MillionTest extends DelfosTest {

    public MovieLens1MillionTest() {
    }

    /**
     * Test of EPinionsDatasetLoader method, of class MovieLens1Million.
     */
    @Test
    public void testMovieLens1Million() {

        System.out.println("testMovieLens1Million");

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-1m");

        Recommender_DatasetProperties recommender_DatasetProperties = new Recommender_DatasetProperties();

        recommender_DatasetProperties.buildRecommendationModel(datasetLoader);

        Global.showInfoMessage("Showing statistics about the ratings dataset\n");
        Global.showInfoMessage("Num ratings " + datasetLoader.getRatingsDataset().getNumRatings() + "\n");
        Global.showInfoMessage("Num users   " + datasetLoader.getRatingsDataset().allUsers().size() + "\n");
        Global.showInfoMessage("Num items   " + datasetLoader.getRatingsDataset().allRatedItems().size() + "\n");

        Global.showInfoMessage("Showing statistics about the dataset\n");
        Global.showInfoMessage("Num ratings " + datasetLoader.getRatingsDataset().getNumRatings() + "\n");
        Global.showInfoMessage("Num users   " + datasetLoader.getUsersDataset().size() + "\n");
        Global.showInfoMessage("Num items   " + datasetLoader.getContentDataset().size() + "\n");

        int minimumUserRatings = 5;
        int minimumItemRatings = 5;

        RatingsDataset<? extends Rating> filteredRatings = RatingsDataset_restrinctNumRatings
                .buildFilteringRatingsUntilAllConditionsAreSatisfied(
                        datasetLoader,
                        minimumUserRatings,
                        minimumItemRatings);

        DatasetLoaderGivenRatingsDataset<? extends Rating> filteredDataset = new DatasetLoaderGivenRatingsDataset<>(datasetLoader, filteredRatings);

        new Recommender_DatasetProperties().buildRecommendationModel(filteredDataset);

    }

}
