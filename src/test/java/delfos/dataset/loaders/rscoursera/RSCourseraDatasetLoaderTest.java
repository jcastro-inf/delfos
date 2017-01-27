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

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.tags.TagsDataset;
import delfos.dataset.basic.user.UsersDataset;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class RSCourseraDatasetLoaderTest {

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
}
