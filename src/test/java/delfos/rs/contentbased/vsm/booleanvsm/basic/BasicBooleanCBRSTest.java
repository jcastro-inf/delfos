package delfos.rs.contentbased.vsm.booleanvsm.basic;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.mockdatasets.MockContentDataset;
import delfos.dataset.mockdatasets.MockRatingsDataset;
import delfos.rs.contentbased.vsm.booleanvsm.SparseVector;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class BasicBooleanCBRSTest extends DelfosTest {

    public BasicBooleanCBRSTest() {
    }

    public static class MockDatasetLoader extends DatasetLoaderAbstract implements ContentDatasetLoader {

        @Override
        public RatingsDataset<? extends Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
            return new MockRatingsDataset();
        }

        @Override
        public ContentDataset getContentDataset() throws CannotLoadContentDataset {
            return new MockContentDataset();
        }
    }

    /**
     * Test of buildRecommendationModel method, of class BasicBooleanCBRS.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testBuild() throws Exception {
        // TODO implement this test
        if (1 == 1) {
            fail("You must implement this test.");
            return;
        }

        DatasetLoader<? extends Rating> datasetLoader = new MockDatasetLoader();
        BasicBooleanCBRS instance = new BasicBooleanCBRS();
        BasicBooleanCBRSModel expResult = null;
        BasicBooleanCBRSModel result = instance.buildRecommendationModel(datasetLoader);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of recommendOnly method, of class BasicBooleanCBRS.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testRecommendOnly() throws Exception {

        // TODO implement this test
        if (1 == 1) {
            fail("You must implement this test.");
            return;
        }

        DatasetLoader<? extends Rating> datasetLoader = null;
        BasicBooleanCBRSModel model = null;
        SparseVector<Long> userProfile = null;
        Collection<Long> candidateItems = null;
        BasicBooleanCBRS instance = new BasicBooleanCBRS();
        Collection<Recommendation> expResult = null;
        Collection<Recommendation> result = instance.recommendOnly(datasetLoader, model, userProfile, candidateItems);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeUserProfile method, of class BasicBooleanCBRS.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMakeUserProfile() throws Exception {
        // TODO implement this test
        if (1 == 1) {
            fail("You must implement this test.");
            return;
        }

        int idUser = 0;
        DatasetLoader<? extends Rating> datasetLoader = null;
        BasicBooleanCBRSModel model = null;
        BasicBooleanCBRS instance = new BasicBooleanCBRS();
        SparseVector expResult = null;
        SparseVector result = instance.makeUserProfile(idUser, datasetLoader, model);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.

    }
}
