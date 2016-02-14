package delfos.rs.contentbased.vsm.booleanvsm;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 09-oct-2013
 */
public class MockDatasetLoader extends DatasetLoaderAbstract {

    private static final long serialVersionUID = 1L;

    private MockRatingsDataset mockRatingsDataset = null;

    @Override
    public RatingsDataset<? extends Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (mockRatingsDataset == null) {
            mockRatingsDataset = new MockRatingsDataset();
        }
        return mockRatingsDataset;
    }
}
