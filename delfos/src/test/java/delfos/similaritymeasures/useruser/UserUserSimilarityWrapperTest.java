package delfos.similaritymeasures.useruser;

import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;

/**
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class UserUserSimilarityWrapperTest {

    private static MockDatasetLoader_UserUserSimilarity datasetLoader;

    public UserUserSimilarityWrapperTest() {
    }

    @BeforeClass
    public static void beforeClass() {
        datasetLoader = new MockDatasetLoader_UserUserSimilarity();
    }

    @Test
    public void testWrapper_PearsonCorrelationCoefficient() throws Exception {
        System.out.println("testWrapper_PearsonCorrelationCoefficient");

        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();
        int idUser1 = 54;
        int idUser2 = 643;
        UserUserSimilarityWrapper instance = new UserUserSimilarityWrapper(pcc);
        double expResult = 0.241746;
        double result = instance.similarity(datasetLoader, idUser1, idUser2);
        assertEquals(expResult, result, 0.000001);
    }

    @Test
    public void testWrapper_CosineCorrelationCoefficient() throws Exception {
        System.out.println("testWrapper_CosineCorrelationCoefficient");

        BasicSimilarityMeasure basicSimilarityMeasure = new CosineCoefficient();
        int idUser1 = 54;
        int idUser2 = 643;
        UserUserSimilarityWrapper instance = new UserUserSimilarityWrapper(basicSimilarityMeasure);
        double expResult = 0.948563;
        double result = instance.similarity(datasetLoader, idUser1, idUser2);
        assertEquals(expResult, result, 0.000001);
    }

}
