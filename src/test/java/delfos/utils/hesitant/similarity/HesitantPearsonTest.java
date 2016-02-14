package delfos.utils.hesitant.similarity;

import delfos.utils.hesitant.HesitantValuation;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Paper: T. González-Arteaga, J.C.R. Alcantud, R. de Andrés Calle: New
 * correlation coefficients for hesitant fuzzy sets (2015) IFSA-EUSFLAT.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class HesitantPearsonTest {

    public HesitantPearsonTest() {
    }

    @Test
    public void testExample1() {

        HesitantValuation<String, Double> profileA = new HesitantValuation<>(Arrays.asList(
                new HesitantValuation.HesitantSingleValuation<>("s1", 0.7, 0.5),
                new HesitantValuation.HesitantSingleValuation<>("s2", 0.9, 0.8, 0.6),
                new HesitantValuation.HesitantSingleValuation<>("s3", 0.5, 0.4, 0.2)
        ));
        HesitantValuation<String, Double> profileB = new HesitantValuation<>(Arrays.asList(
                new HesitantValuation.HesitantSingleValuation<>("s1", 0.4, 0.2),
                new HesitantValuation.HesitantSingleValuation<>("s2", 0.8, 0.5, 0.4),
                new HesitantValuation.HesitantSingleValuation<>("s3", 0.7, 0.6, 0.3)
        ));

        HesitantPearson pearson = new HesitantPearson();

        int expResult_sizeR_HFS = 22;
        assertEquals(expResult_sizeR_HFS, HesitantPearson.getSizeR_HFS(profileA, profileB));

        double expResult_meanH_X = 0.57272727;
        assertEquals(expResult_meanH_X, pearson.getMeanHX(profileA, profileB), 0.00001);

        double expResult_meanH_Y = 0.50454545;
        assertEquals(expResult_meanH_Y, pearson.getMeanHY(profileA, profileB), 0.00001);

        double expResult_SCC_XY = 0.03272727;
        assertEquals(expResult_SCC_XY, pearson.getSCC(profileA, profileB), 0.00001);

        double expResult_SSH_X = 1.04363636;
        assertEquals(expResult_SSH_X, pearson.getSSHX(profileA, profileB), 0.00001);

        double expResult_SSH_Y = 0.769545454;
        assertEquals(expResult_SSH_Y, pearson.getSSHY(profileA, profileB), 0.00001);

        double expResult_pearson = 0.036518932626;
        assertEquals(expResult_pearson, pearson.similarity(profileA, profileB), 0.00001);
    }

    @Test
    public void testExample2() {

        HesitantValuation<String, Double> profileA = new HesitantValuation<>(Arrays.asList(
                new HesitantValuation.HesitantSingleValuation<>("s1", 0.05, 0.1, 0.15),
                new HesitantValuation.HesitantSingleValuation<>("s2", 0.2, 0.3, 0.4),
                new HesitantValuation.HesitantSingleValuation<>("s3", 0.4, 0.5, 0.6)
        ));
        HesitantValuation<String, Double> profileB = new HesitantValuation<>(Arrays.asList(
                new HesitantValuation.HesitantSingleValuation<>("s1", 0.3, 0.4, 0.5),
                new HesitantValuation.HesitantSingleValuation<>("s2", 0.5, 0.6, 0.7),
                new HesitantValuation.HesitantSingleValuation<>("s3", 0.7, 0.8, 0.9)
        ));

        HesitantPearson pearson = new HesitantPearson();

        int expResult_sizeR_HFS = 27;
        assertEquals(expResult_sizeR_HFS, HesitantPearson.getSizeR_HFS(profileA, profileB));

        double expResult_meanH_X = 0.3;
        assertEquals(expResult_meanH_X, pearson.getMeanHX(profileA, profileB), 0.00001);

        double expResult_meanH_Y = 0.6;
        assertEquals(expResult_meanH_Y, pearson.getMeanHY(profileA, profileB), 0.00001);

        double expResult_SCC_XY = 0.72;
        assertEquals(expResult_SCC_XY, pearson.getSCC(profileA, profileB), 0.00001);

        double expResult_SSH_X = 0.855;
        assertEquals(expResult_SSH_X, pearson.getSSHX(profileA, profileB), 0.00001);

        double expResult_SSH_Y = 0.9;
        assertEquals(expResult_SSH_Y, pearson.getSSHY(profileA, profileB), 0.00001);

        double expResult_pearson = 0.8207827;
        assertEquals(expResult_pearson, pearson.similarity(profileA, profileB), 0.00001);
    }

    @Test
    public void testExample3() {

        HesitantValuation<String, Double> profileA = new HesitantValuation<>(Arrays.asList(
                new HesitantValuation.HesitantSingleValuation<>("s1", 0.05, 0.1, 0.15),
                new HesitantValuation.HesitantSingleValuation<>("s2", 0.2, 0.3, 0.4),
                new HesitantValuation.HesitantSingleValuation<>("s3", 0.4, 0.5, 0.6)
        ));
        HesitantValuation<String, Double> profileB = new HesitantValuation<>(Arrays.asList(
                new HesitantValuation.HesitantSingleValuation<>("s1", 0.7, 0.8, 0.9),
                new HesitantValuation.HesitantSingleValuation<>("s2", 0.5, 0.6, 0.7),
                new HesitantValuation.HesitantSingleValuation<>("s3", 0.3, 0.4, 0.5)
        ));

        HesitantPearson pearson = new HesitantPearson();

        int expResult_sizeR_HFS = 27;
        assertEquals(expResult_sizeR_HFS, HesitantPearson.getSizeR_HFS(profileA, profileB));

        double expResult_meanH_X = 0.3;
        assertEquals(expResult_meanH_X, pearson.getMeanHX(profileA, profileB), 0.00001);

        double expResult_meanH_Y = 0.6;
        assertEquals(expResult_meanH_Y, pearson.getMeanHY(profileA, profileB), 0.00001);

        double expResult_SCC_XY = -0.72;
        assertEquals(expResult_SCC_XY, pearson.getSCC(profileA, profileB), 0.00001);

        double expResult_SSH_X = 0.855;
        assertEquals(expResult_SSH_X, pearson.getSSHX(profileA, profileB), 0.00001);

        double expResult_SSH_Y = 0.9;
        assertEquals(expResult_SSH_Y, pearson.getSSHY(profileA, profileB), 0.00001);

        double expResult_pearson = -0.82078727;
        assertEquals(expResult_pearson, pearson.similarity(profileA, profileB), 0.00001);
    }

    @Test
    public void testExample4_test() {

        HesitantValuation<String, Double> profileA = new HesitantValuation<>(Arrays.asList(
                new HesitantValuation.HesitantSingleValuation<>("s1", 0.05, 0.1, 0.15),
                new HesitantValuation.HesitantSingleValuation<>("s2", 0.2, 0.3, 0.4),
                new HesitantValuation.HesitantSingleValuation<>("s3", 0.4, 0.5, 0.6)
        ));
        HesitantValuation<String, Double> profileB = new HesitantValuation<>(Arrays.asList(
                new HesitantValuation.HesitantSingleValuation<>("s1", 0.7, 0.8, 0.8),
                new HesitantValuation.HesitantSingleValuation<>("s2", 0.5, 0.5, 0.5),
                new HesitantValuation.HesitantSingleValuation<>("s3", 0.3, 0.3, 0.3)
        ));

        HesitantPearson hesitantPearson = new HesitantPearson();
        double similarity = hesitantPearson.similarity(profileA, profileB);
        System.out.println(similarity);

    }
}
