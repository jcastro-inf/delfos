package delfos.dataset.datasetloaders.movilens.ml1m;

import delfos.dataset.loaders.movilens.ml1m.MovieLens1Million;
import org.junit.Test;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class MovieLens1MillionTest extends DelfosTest {

    public MovieLens1MillionTest() {
    }

    /**
     * Test of getRatingsDataset method, of class MovieLens1Million.
     */
    @Test
    public void testDatasetLoader() {
        MovieLens1Million ml_1m = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-1m", MovieLens1Million.class);

        RatingsDataset<RatingWithTimestamp> ratingsDataset = ml_1m.getRatingsDataset();
        ContentDataset contentDataset = ml_1m.getContentDataset();
        UsersDataset usersDataset = ml_1m.getUsersDataset();
    }
}
