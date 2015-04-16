package delfos.dataset.memory.validationdatasets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.test.RatingsDatasetTest;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.TestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.dataset.util.DatasetPrinter;

/**
 * Test para probar la clase {@link TestRatingsDataset}.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 06-Mar-2013
 */
public class TestRatingsDatasetTest extends DelfosTest {

    public TestRatingsDatasetTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Comprueba que las productos que se indican como test son visibles y el
     * resto están ocultas.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSetRating() throws Exception {

        int numUsers = 5;
        int numItems = 5;
        Number rating = 3;

        List<Rating> ratings = new ArrayList<>(numUsers * numItems);
        for (int idUser = 1; idUser <= numUsers; idUser++) {
            for (int idItem = 1; idItem <= numItems; idItem++) {
                ratings.add(new Rating(idUser, idItem, rating));
            }
        }
        BothIndexRatingsDataset ratingsDataset = new BothIndexRatingsDataset(ratings);

        System.out.println(DatasetPrinter.printCompactRatingTable(ratingsDataset));

        Map<Integer, Set<Integer>> testRatings = new TreeMap<>();
        Set<Integer> listaItems = new TreeSet<>();
        listaItems.add(1);
        listaItems.add(2);
        testRatings.put(3, listaItems);

        TestRatingsDataset testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(ratingsDataset, testRatings);

        System.out.println(DatasetPrinter.printCompactRatingTable(testRatingsDataset));
        assert testRatingsDataset.getRating(3, 1).ratingValue.intValue() == 3;
        assert testRatingsDataset.getRating(3, 2).ratingValue.intValue() == 3;

        try {
            Rating ret = testRatingsDataset.getRating(3, 3);
            assert ret == null;
        } catch (ItemNotFound ex) {
            //Si salta la excepción, también es correcto el código
        }
    }

    @Test
    public void testSize() throws UserNotFound, ItemNotFound {
        //Dataset Completo
        int numUsers = 5;
        int numItems = 5;
        Number rating = 3;

        List<Rating> ratings = new ArrayList<>(numUsers * numItems);
        for (int idUser = 1; idUser <= numUsers; idUser++) {
            for (int idItem = 1; idItem <= numItems; idItem++) {
                ratings.add(new Rating(idUser, idItem, rating));
            }
        }
        BothIndexRatingsDataset originalDataset = new BothIndexRatingsDataset(ratings);

        Assert.assertEquals("Original dataset doesn't have 25 ratings", 25, originalDataset.getNumRatings());

        Map<Integer, Set<Integer>> testSet = new TreeMap<>();
        testSet.put(1, new TreeSet<>());
        testSet.put(3, new TreeSet<>());
        testSet.get(1).add(1);
        testSet.get(1).add(5);
        testSet.get(3).add(3);

        TestRatingsDataset testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(originalDataset, testSet);
        Assert.assertEquals("Test set doesn't have 3 ratings", 3, testRatingsDataset.getNumRatings());
    }

    @Test
    public void testIterator() throws UserNotFound, ItemNotFound {
        //Dataset Completo
        int numUsers = 5;
        int numItems = 5;
        Number rating = 3;

        List<Rating> ratings = new ArrayList<>(numUsers * numItems);
        for (int idUser = 1; idUser <= numUsers; idUser++) {
            for (int idItem = 1; idItem <= numItems; idItem++) {
                ratings.add(new Rating(idUser, idItem, rating));
            }
        }
        BothIndexRatingsDataset<Rating> originalDataset = new BothIndexRatingsDataset<>(ratings);

        {
            int originalDatasetSize_accordingToIterator = 0;
            for (Rating r : originalDataset) {
                originalDatasetSize_accordingToIterator++;
            }
            Assert.assertEquals(25, originalDatasetSize_accordingToIterator);
        }

        Map<Integer, Set<Integer>> testSet = new TreeMap<>();
        testSet.put(1, new TreeSet<>());
        testSet.put(3, new TreeSet<>());
        testSet.get(1).add(1);
        testSet.get(1).add(5);
        testSet.get(3).add(3);

        TestRatingsDataset<Rating> testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(originalDataset, testSet);
        {
            int testDatasetSize_accordingToIterator = 0;
            for (Rating r : testRatingsDataset) {
                testDatasetSize_accordingToIterator++;
            }
            Assert.assertEquals(3, testDatasetSize_accordingToIterator);
        }
    }

    @Test
    public void testNumberOfRatings() throws UserNotFound, ItemNotFound {
        Global.setVerbose();

        Random random = new Random(0);
        //Dataset Completo
        RatingsDataset<? extends Rating> originalDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(50, 50, 0.5);

        int numRatings_usingMet = 0;
        int numRatings_usingIte = 0;
        final int numRatings_usingGet = originalDataset.getNumRatings();

        for (int idUser : originalDataset.allUsers()) {
            try {
                for (int idItem : originalDataset.getUserRated(idUser)) {
                    numRatings_usingMet++;
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        ArrayList<Rating> ratings = new ArrayList<>(originalDataset.getNumRatings());
        for (Rating r : originalDataset) {
            ratings.add(r);
            numRatings_usingIte++;
        }

        Assert.assertEquals(numRatings_usingGet, numRatings_usingMet);
        Assert.assertEquals(numRatings_usingGet, numRatings_usingIte);
        Assert.assertEquals(numRatings_usingIte, numRatings_usingMet);

        Assert.assertEquals(
                "The size (" + originalDataset.getNumRatings() + ")is different "
                + "to the number of elements retrieved with iterator (" + ratings.size() + ")",
                ratings.size(),
                originalDataset.getNumRatings());

        Map<Integer, Set<Integer>> testRatings = new TreeMap<>();

        Collection<Integer> allUsers = originalDataset.allUsers();
        int numTestRatings = 0;
        while (numTestRatings < 5) {
            int idUser = (Integer) allUsers.toArray()[random.nextInt(allUsers.size())];
            if (!testRatings.containsKey(idUser)) {

                testRatings.put(idUser, new TreeSet<>());

            }
            Set<Integer> notInTrain = new TreeSet<>(originalDataset.getUserRated(idUser));
            notInTrain.removeAll(testRatings.get(idUser));
            if (notInTrain.isEmpty()) {
                allUsers.remove(idUser);
            } else {
                int idItem = (Integer) notInTrain.toArray()[random.nextInt(notInTrain.size())];

                testRatings.get(idUser).add(idItem);
                numTestRatings++;
            }
        }

        TestRatingsDataset<? extends Rating> testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(originalDataset, testRatings);
        int testRatings_get = testRatingsDataset.getNumRatings();
        int testRatings_ite = 0;
        int testRatings_met = 0;
        for (Rating r : testRatingsDataset) {
            testRatings_ite++;
        }

        for (int idUser : testRatingsDataset.allUsers()) {
            for (int idItem : testRatingsDataset.getUserRated(idUser)) {
                testRatings_met++;
            }
        }

        Assert.assertEquals(testRatings_get, testRatings_met);
        Assert.assertEquals(testRatings_get, testRatings_ite);
        Assert.assertEquals(testRatings_ite, testRatings_met);
    }

    @Test
    public void testDataset() throws Exception {

        Global.setVerbose();

        Random random = new Random(0);
        //Dataset Completo
        RatingsDataset<? extends Rating> originalDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(50, 50, 0.5);

        Map<Integer, Set<Integer>> testRatings = new TreeMap<>();

        Collection<Integer> allUsers = originalDataset.allUsers();
        int numTestRatings = 0;
        while (numTestRatings < 5) {
            int idUser = (Integer) allUsers.toArray()[random.nextInt(allUsers.size())];
            if (!testRatings.containsKey(idUser)) {

                testRatings.put(idUser, new TreeSet<>());

            }
            Set<Integer> notInTrain = new TreeSet<>(originalDataset.getUserRated(idUser));
            notInTrain.removeAll(testRatings.get(idUser));
            if (notInTrain.isEmpty()) {
                allUsers.remove(idUser);
            } else {
                int idItem = (Integer) notInTrain.toArray()[random.nextInt(notInTrain.size())];

                testRatings.get(idUser).add(idItem);
                numTestRatings++;
            }
        }

        TestRatingsDataset testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(originalDataset, testRatings);

        RatingsDatasetTest.testIteratorEqualsOtherMethods(testRatingsDataset);
    }
}
