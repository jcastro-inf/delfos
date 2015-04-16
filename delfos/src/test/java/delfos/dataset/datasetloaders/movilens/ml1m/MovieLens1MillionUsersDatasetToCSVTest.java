package delfos.dataset.datasetloaders.movilens.ml1m;

import delfos.dataset.loaders.movilens.ml1m.MovieLens1Million;
import delfos.dataset.loaders.movilens.ml1m.MovieLens1MillionUsersDatasetToCSV;
import java.io.File;
import org.junit.Test;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.user.UsersDataset;

/**
 *
 * @author jcastro
 */
public class MovieLens1MillionUsersDatasetToCSVTest extends DelfosTest {

    public MovieLens1MillionUsersDatasetToCSVTest() {
    }

    /**
     * Test of readUsersDataset method, of class
     * MovieLens1MillionUsersDatasetToCSV.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testReadUsersDataset() throws Exception {
        System.out.println("readUsersDataset");

        MovieLens1Million ml_1m = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-1m", MovieLens1Million.class);
        File usersCSVFile = ml_1m.getUsersFile();

        MovieLens1MillionUsersDatasetToCSV instance = new MovieLens1MillionUsersDatasetToCSV();
        UsersDataset result = instance.readUsersDataset(usersCSVFile);
    }

}
