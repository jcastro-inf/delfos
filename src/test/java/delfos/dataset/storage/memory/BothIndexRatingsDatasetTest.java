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
package delfos.dataset.storage.memory;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.rs.collaborativefiltering.Recommender_DatasetProperties;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class BothIndexRatingsDatasetTest extends DelfosTest {

    public BothIndexRatingsDatasetTest() {
    }

    @Test
    public void testCreateFromCollectionOfRatingsMovieLens100k() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        BothIndexRatingsDataset<? extends Rating> newRatingsDataset = new BothIndexRatingsDataset<>(datasetLoader.getRatingsDataset());

        new Recommender_DatasetProperties().buildRecommendationModel(new DatasetLoaderGivenRatingsDataset<>(datasetLoader, newRatingsDataset));

        Assert.assertEquals("ml-100k dataset has 943 users", 943, newRatingsDataset.allUsers().size());
        Assert.assertEquals("ml-100k dataset has 1682 items", 1682, newRatingsDataset.allRatedItems().size());
        Assert.assertEquals("ml-100k dataset has 100000 ratings", 100000, newRatingsDataset.getNumRatings());

    }

    @Test
    public void testCreateFromCollectionOfRatingsMovieLens1m() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-1m");

        BothIndexRatingsDataset<? extends Rating> newRatingsDataset = new BothIndexRatingsDataset<>(datasetLoader.getRatingsDataset());

        new Recommender_DatasetProperties().buildRecommendationModel(new DatasetLoaderGivenRatingsDataset<>(datasetLoader, newRatingsDataset));

        Assert.assertEquals("ml-100k dataset has 943 users", 6040, newRatingsDataset.allUsers().size());
        Assert.assertEquals("ml-100k dataset has 1682 items", 3706, newRatingsDataset.allRatedItems().size());
        Assert.assertEquals("ml-100k dataset has 100000 ratings", 1000209, newRatingsDataset.getNumRatings());

    }

}
