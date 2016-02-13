package delfos.dataset.datasetloaders.movilens.ml100k;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import org.junit.Test;

/**
 *
 * @version 08-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class MovieLens100kTest extends DelfosTest {

    public MovieLens100kTest() {
    }

    /**
     * Test of getRatingsDataset method, of class MovieLens100k.
     */
    @Test
    public void testGetRatingsDataset() {
        MovieLens100k instance = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        RatingsDataset<Rating> result = instance.getRatingsDataset();
    }

    /**
     * Test of getContentDataset method, of class MovieLens100k.
     */
    @Test
    public void testGetContentDataset() {
        MovieLens100k instance = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        ContentDataset result = instance.getContentDataset();
    }

    /**
     * Test of getUsersDataset method, of class MovieLens100k.
     */
    @Test
    public void testGetUsersDataset() {
        MovieLens100k instance = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        UsersDataset result = instance.getUsersDataset();
    }
}
