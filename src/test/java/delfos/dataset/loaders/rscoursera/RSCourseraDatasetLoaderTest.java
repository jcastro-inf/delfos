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
package delfos.dataset.loaders.rscoursera;

import delfos.common.datastructures.histograms.HistogramNumbersSmart;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.tags.TagsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV_JavaCSV20;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class RSCourseraDatasetLoaderTest extends DelfosTest {

    public RSCourseraDatasetLoaderTest() {
    }

    /**
     * Test of getRatingsDataset method, of class RSCourseraDatasetLoader.
     */
    @Test
    public void testGetRatingsDataset() {
        System.out.println("getRatingsDataset");
        RSCourseraDatasetLoader instance = getRSCourseraDatasetLoader();

        RatingsDataset<Rating> ratingsDataset = instance.getRatingsDataset();

        Assert.assertEquals("Num ratings not correct", 338355, ratingsDataset.getNumRatings());
        Assert.assertEquals("Num users not correct", 5564, ratingsDataset.allUsers().size());
        Assert.assertEquals("Num items not correct", 100, ratingsDataset.allRatedItems().size());
    }

    /**
     * Test of getContentDataset method, of class RSCourseraDatasetLoader.
     */
    @Test
    public void testGetContentDataset() {
        System.out.println("getContentDataset");
        RSCourseraDatasetLoader instance = getRSCourseraDatasetLoader();

        ContentDataset contentDataset = instance.getContentDataset();

        Assert.assertEquals("Num items not correct", 100, contentDataset.size());
    }

    /**
     * Test of getUsersDataset method, of class RSCourseraDatasetLoader.
     */
    @Test
    public void testGetUsersDataset() {
        System.out.println("getUsersDataset");
        RSCourseraDatasetLoader instance = getRSCourseraDatasetLoader();

        UsersDataset usersDataset = instance.getUsersDataset();

        Assert.assertEquals("Num users not correct", 5564, usersDataset.size());
    }

    /**
     * Test of getTagsDataset method, of class RSCourseraDatasetLoader.
     */
    @Test
    public void testGetTagsDataset() {
        System.out.println("getTagsDataset");
        RSCourseraDatasetLoader instance = getRSCourseraDatasetLoader();

        TagsDataset tagsDataset = instance.getTagsDataset();

        long count = tagsDataset.stream().count();

        Assert.assertEquals("Num tags not correct", 41980, count);

    }

    public RSCourseraDatasetLoader getRSCourseraDatasetLoader() {
        RSCourseraDatasetLoader instance = new RSCourseraDatasetLoader();
        instance.setRatingsDatasetFile(new File("temp" + File.separator + "rs-coursera" + File.separator
                + "ratings.csv"));
        instance.setContentDatasetFile(new File("temp" + File.separator + "rs-coursera" + File.separator
                + "movie-titles.csv"));
        instance.setUsersDatasetFile(new File("temp" + File.separator + "rs-coursera" + File.separator
                + "users.csv"));
        instance.setTagsDatasetFile(new File("temp" + File.separator + "rs-coursera" + File.separator
                + "movie-tags.csv"));
        return instance;
    }

    @Test
    public void generatePartitions() throws IOException {
        RSCourseraDatasetLoader rsCourseraDatasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("rs-coursera", RSCourseraDatasetLoader.class);

        CrossFoldValidation_Ratings cfv = new CrossFoldValidation_Ratings();

        cfv.setNumberOfPartitions(5);
        cfv.setSeedValue(0);

        String directory = DelfosTest.getTemporalDirectoryForTest(this.getClass()).getPath()
                + File.separator;

        PairOfTrainTestRatingsDataset[] shuffle = cfv.shuffle(rsCourseraDatasetLoader);

        RatingsDatasetToCSV ratingsDatasetToCSV = new RatingsDatasetToCSV_JavaCSV20();

        ratingsDatasetToCSV.writeDataset(shuffle[0].train, directory + "ratings_train_0.csv");
        ratingsDatasetToCSV.writeDataset(shuffle[0].test, directory + "ratings_test_0.csv");

        ratingsDatasetToCSV.writeDataset(shuffle[1].train, directory + "ratings_train_1.csv");
        ratingsDatasetToCSV.writeDataset(shuffle[1].test, directory + "ratings_test_1.csv");

        ratingsDatasetToCSV.writeDataset(shuffle[2].train, directory + "ratings_train_2.csv");
        ratingsDatasetToCSV.writeDataset(shuffle[2].test, directory + "ratings_test_2.csv");

        ratingsDatasetToCSV.writeDataset(shuffle[3].train, directory + "ratings_train_3.csv");
        ratingsDatasetToCSV.writeDataset(shuffle[3].test, directory + "ratings_test_3.csv");

        ratingsDatasetToCSV.writeDataset(shuffle[4].train, directory + "ratings_train_4.csv");
        ratingsDatasetToCSV.writeDataset(shuffle[4].test, directory + "ratings_test_4.csv");

    }

    @Test
    public void userHistogram() {

        DatasetLoader<? extends Rating> rsCourseraDatasetLoader = ConfiguredDatasetsFactory.getInstance()
                .getDatasetLoader("ml-100k");

        HistogramNumbersSmart histogramNumbersSmart = new HistogramNumbersSmart(1);

        Set<Integer> allUsers = rsCourseraDatasetLoader.getRatingsDataset().allUsers();

        allUsers.stream().forEach(user -> {

            int numRatings = rsCourseraDatasetLoader
                    .getRatingsDataset()
                    .getUserRated(user)
                    .size();

            histogramNumbersSmart.addValue(numRatings);

        });

        histogramNumbersSmart.printHistogram(System.out);

    }
}
