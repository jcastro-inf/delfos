package delfos.dataset.datasetloaders.movilens.ml1m;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.loaders.movilens.ml1m.MovieLens1Million;
import delfos.dataset.loaders.movilens.ml1m.MovieLens1MillionContentDatasetToCSV;
import java.io.File;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class MovieLens1MillionContentDatasetToCSVTest extends DelfosTest {

    public MovieLens1MillionContentDatasetToCSVTest() {
    }

    /**
     * Test of readContentDataset method, of class
     * MovieLens1MillionContentDatasetToCSV.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testReadContentDataset() throws Exception {

        MovieLens1Million ml_1m = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-1m", MovieLens1Million.class);

        File contentCSVFile = ml_1m.getContentFile();
        MovieLens1MillionContentDatasetToCSV instance = new MovieLens1MillionContentDatasetToCSV();
        ContentDataset result = instance.readContentDataset(contentCSVFile);
    }

}
