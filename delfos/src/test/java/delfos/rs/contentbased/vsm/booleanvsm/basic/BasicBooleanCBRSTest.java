package delfos.rs.contentbased.vsm.booleanvsm.basic;

import delfos.rs.contentbased.vsm.booleanvsm.basic.BasicBooleanCBRS;
import delfos.rs.contentbased.vsm.booleanvsm.basic.BasicBooleanCBRSModel;
import java.util.Collection;
import java.util.List;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;
import static org.junit.Assert.*;
import delfos.dataset.mockdatasets.MockContentDataset;
import delfos.dataset.mockdatasets.MockRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.recommendation.Recommendation;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;

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
     * Test of build method, of class BasicBooleanCBRS.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testBuild() throws Exception {
        // TODO implement this test
        if (1 == 1) {
            System.out.println("You must implement this test.");
            return;
        }

        System.out.println("build");
        DatasetLoader<? extends Rating> datasetLoader = new MockDatasetLoader();
        BasicBooleanCBRS instance = new BasicBooleanCBRS();
        BasicBooleanCBRSModel expResult = null;
        BasicBooleanCBRSModel result = instance.build(datasetLoader);
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
            System.out.println("You must implement this test.");
            return;
        }

        System.out.println("recommendOnly");
        DatasetLoader<? extends Rating> datasetLoader = null;
        BasicBooleanCBRSModel model = null;
        SparseVector userProfile = null;
        Collection<Integer> candidateItems = null;
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
            System.out.println("You must implement this test.");
            return;
        }

        System.out.println("makeUserProfile");
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
