package delfos.utils.fuzzyclustering;

import delfos.utils.fuzzyclustering.distance.CosineDistance_noNull;
import delfos.utils.fuzzyclustering.vector.DataVector;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.Test;

/**
 *
 * @version 16-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class FuzzyCMeansTest {

    public FuzzyCMeansTest() {
    }

    /**
     * Test of executeClustering method, of class FuzzyCMeans.
     */
    @Test
    public void testExecuteClusteringCompleteMatrix() {
        System.out.println("testExecuteClusteringCompleteMatrix");

        Map<MockIdUser, DataVector<MockIdItem>> inputs = getCompleteMatrix();
        int numClusters = 2;
        int numRounds = 50;
        double fuzziness = 2;

        FuzzyCMeans<MockIdUser, MockIdItem> instance = new FuzzyCMeans<>(new CosineDistance_noNull());
        Set<FuzzyCluster<MockIdItem>> clusters = instance.executeClustering(inputs, numClusters, numRounds, fuzziness);

        System.out.println("======================================");
        System.out.println("Final clusters:");
        for (FuzzyCluster<MockIdItem> cluster : clusters) {
            System.out.println(cluster);
        }
        System.out.println("======================================");

        System.out.println("testExecuteClusteringCompleteMatrix finish");
    }

    /**
     * Test of executeClustering method, of class FuzzyCMeans.
     */
    @Test
    public void testExecuteClusteringIncompleteMatrix() {
        System.out.println("testExecuteClusteringIncompleteMatrix");

        Map<MockIdUser, DataVector<MockIdItem>> inputs = getIncompleteMatrix();
        int numClusters = 2;
        int numRounds = 50;
        double fuzziness = 2;

        FuzzyCMeans<MockIdUser, MockIdItem> instance = new FuzzyCMeans<>(new CosineDistance_noNull());
        Set<FuzzyCluster<MockIdItem>> clusters = instance.executeClustering(inputs, numClusters, numRounds, fuzziness);

        System.out.println("======================================");
        System.out.println("Final clusters:");
        for (FuzzyCluster<MockIdItem> cluster : clusters) {
            System.out.println(cluster);
        }
        System.out.println("======================================");

        System.out.println("testExecuteClusteringIncompleteMatrix finished");
    }

    /**
     * Test of executeClustering method, of class FuzzyCMeans.
     */
    @Test
    public void testExecuteClusteringCompleteMatrixAndGetCrispClustering() {
        System.out.println("testExecuteClusteringCompleteMatrix");

        Map<MockIdUser, DataVector<MockIdItem>> inputs = getCompleteMatrix();
        int numClusters = 2;
        int numRounds = 50;
        double fuzziness = 2;

        FuzzyCMeans<MockIdUser, MockIdItem> instance = new FuzzyCMeans<>(new CosineDistance_noNull());
        List<List<MockIdUser>> clusters = instance.getCrispClusteredItems(inputs, numClusters, numRounds, fuzziness);

        System.out.println("======================================");
        System.out.println("Final clusters:");
        for (List<MockIdUser> cluster : clusters) {
            System.out.println(cluster);
        }
        System.out.println("======================================");

        System.out.println("testExecuteClusteringCompleteMatrix finish");
    }

    public Map<MockIdUser, DataVector<MockIdItem>> getCompleteMatrix() {
        Map<MockIdUser, DataVector<MockIdItem>> completeMatrix = new TreeMap<>();
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
            dataVector.put(new MockIdItem(1), 4.0);
            dataVector.put(new MockIdItem(2), 5.0);
            dataVector.put(new MockIdItem(3), 5.0);
            dataVector.put(new MockIdItem(4), 4.0);
            dataVector.put(new MockIdItem(5), 3.0);
            completeMatrix.put(new MockIdUser(10), new DataVector<>(dataVector));
        }
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
            dataVector.put(new MockIdItem(1), 4.0);
            dataVector.put(new MockIdItem(2), 1.0);
            dataVector.put(new MockIdItem(3), 1.0);
            dataVector.put(new MockIdItem(4), 2.0);
            dataVector.put(new MockIdItem(5), 2.0);
            completeMatrix.put(new MockIdUser(20), new DataVector<>(dataVector));
        }
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
            dataVector.put(new MockIdItem(1), 2.0);
            dataVector.put(new MockIdItem(2), 3.0);
            dataVector.put(new MockIdItem(3), 3.0);
            dataVector.put(new MockIdItem(4), 3.0);
            dataVector.put(new MockIdItem(5), 3.0);
            completeMatrix.put(new MockIdUser(30), new DataVector<>(dataVector));
        }
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
            dataVector.put(new MockIdItem(1), 1.0);
            dataVector.put(new MockIdItem(2), 1.0);
            dataVector.put(new MockIdItem(3), 5.0);
            dataVector.put(new MockIdItem(4), 1.0);
            dataVector.put(new MockIdItem(5), 1.0);
            completeMatrix.put(new MockIdUser(40), new DataVector<>(dataVector));
        }
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
            dataVector.put(new MockIdItem(1), 1.0);
            dataVector.put(new MockIdItem(2), 5.0);
            dataVector.put(new MockIdItem(3), 5.0);
            dataVector.put(new MockIdItem(4), 5.0);
            dataVector.put(new MockIdItem(5), 1.0);
            completeMatrix.put(new MockIdUser(50), new DataVector<>(dataVector));
        }
        return completeMatrix;
    }

    private Map<MockIdUser, DataVector<MockIdItem>> getIncompleteMatrix() {
        Map<MockIdUser, DataVector<MockIdItem>> incompleteMatrix = new TreeMap<>();
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
            dataVector.put(new MockIdItem(1), 4.0);
            dataVector.put(new MockIdItem(2), 5.0);
            dataVector.put(new MockIdItem(3), 5.0);
//            dataVector.put(new MockIdItem(4), 4.0);
//            dataVector.put(new MockIdItem(5), 3.0);
            incompleteMatrix.put(new MockIdUser(60), new DataVector<>(dataVector));
        }
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
            dataVector.put(new MockIdItem(1), 4.0);
//            dataVector.put(new MockIdItem(2), 1.0);
//            dataVector.put(new MockIdItem(3), 1.0);
//            dataVector.put(new MockIdItem(4), 2.0);
            dataVector.put(new MockIdItem(5), 2.0);
            incompleteMatrix.put(new MockIdUser(70), new DataVector<>(dataVector));
        }
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
//            dataVector.put(new MockIdItem(1), 2.0);
//            dataVector.put(new MockIdItem(2), 3.0);
//            dataVector.put(new MockIdItem(3), 3.0);
            dataVector.put(new MockIdItem(4), 3.0);
            dataVector.put(new MockIdItem(5), 3.0);
            incompleteMatrix.put(new MockIdUser(80), new DataVector<>(dataVector));
        }
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
            dataVector.put(new MockIdItem(1), 1.0);
//            dataVector.put(new MockIdItem(2), 1.0);
            dataVector.put(new MockIdItem(3), 5.0);
            dataVector.put(new MockIdItem(4), 1.0);
//            dataVector.put(new MockIdItem(5), 1.0);
            incompleteMatrix.put(new MockIdUser(90), new DataVector<>(dataVector));
        }
        {
            Map<MockIdItem, Double> dataVector = new TreeMap<>();
//            dataVector.put(new MockIdItem(1), 1.0);
            dataVector.put(new MockIdItem(2), 5.0);
//            dataVector.put(new MockIdItem(3), 5.0);
            dataVector.put(new MockIdItem(4), 5.0);
            dataVector.put(new MockIdItem(5), 1.0);
            incompleteMatrix.put(new MockIdUser(100), new DataVector<>(dataVector));
        }
        return incompleteMatrix;
    }
}
