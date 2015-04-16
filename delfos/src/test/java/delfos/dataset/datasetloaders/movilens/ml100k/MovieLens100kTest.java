package delfos.dataset.datasetloaders.movilens.ml100k;

import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import org.junit.Test;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;

/**
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class MovieLens100kTest extends DelfosTest {

    public MovieLens100kTest() {
    }

    /**
     * Test of getRatingsDataset method, of class MovieLens100k.
     */
    @Test
    public void testGetRatingsDataset() {
        System.out.println("getRatingsDataset");
        MovieLens100k instance = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        RatingsDataset<Rating> result = instance.getRatingsDataset();
    }

    /**
     * Test of getContentDataset method, of class MovieLens100k.
     */
    @Test
    public void testGetContentDataset() {
        System.out.println("getContentDataset");
        MovieLens100k instance = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        ContentDataset result = instance.getContentDataset();
    }

    /**
     * Test of getUsersDataset method, of class MovieLens100k.
     */
    @Test
    public void testGetUsersDataset() {
        System.out.println("getUsersDataset");
        MovieLens100k instance = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        UsersDataset result = instance.getUsersDataset();
    }
}
