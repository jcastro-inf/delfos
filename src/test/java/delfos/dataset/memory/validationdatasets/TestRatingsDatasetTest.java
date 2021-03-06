package delfos.dataset.memory.validationdatasets;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.test.RatingsDatasetTest;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.TestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.dataset.util.DatasetPrinter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test para probar la clase {@link TestRatingsDataset}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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

        Global.showln(DatasetPrinter.printCompactRatingTable(ratingsDataset));

        Item item1 = new Item(1,"Item_1");
        Item item2 = new Item(2,"Item_2");
        User user3 = new User(3,"User_3");

        Map<User, Set<Item>> testRatings = new TreeMap<>();
        Set<Item> listaItems = new TreeSet<>();
        listaItems.add(item1);
        listaItems.add(item2);
        testRatings.put(user3, listaItems);

        TestRatingsDataset testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(ratingsDataset, testRatings);

        Global.showln(DatasetPrinter.printCompactRatingTable(testRatingsDataset));
        assert testRatingsDataset.getRating(3, 1).getRatingValue().intValue() == 3;
        assert testRatingsDataset.getRating(3, 2).getRatingValue().intValue() == 3;

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

        User user1 = new User(1);
        User user3 = new User(3);

        Item item1 = new Item(1);
        Item item3 = new Item(3);
        Item item5 = new Item(5);

        Map<User, Set<Item>> testSet = new TreeMap<>();
        testSet.put(user1, new TreeSet<>());
        testSet.put(user3, new TreeSet<>());
        testSet.get(user1).add(item1);
        testSet.get(user1).add(item5);
        testSet.get(user3).add(item3);

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

        User user1 = new User(1);
        User user3 = new User(3);

        Item item1 = new Item(1);
        Item item3 = new Item(3);
        Item item5 = new Item(5);

        Map<User, Set<Item>> testSet = new TreeMap<>();
        testSet.put(user1, new TreeSet<>());
        testSet.put(user3, new TreeSet<>());
        testSet.get(user1).add(item1);
        testSet.get(user1).add(item5);
        testSet.get(user3).add(item3);

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

        Random random = new Random(0);
        //Dataset Completo
        RatingsDataset<? extends Rating> originalDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(50, 50, 0.5);

        int numRatings_usingMet = 0;
        int numRatings_usingIte = 0;
        final long numRatings_usingGet = originalDataset.getNumRatings();

        for (long idUser : originalDataset.allUsers()) {
            try {
                for (long idItem : originalDataset.getUserRated(idUser)) {
                    numRatings_usingMet++;
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        ArrayList<Rating> ratings = new ArrayList<>((int) originalDataset.getNumRatings());
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

        Map<User, Set<Item>> testRatings = new TreeMap<>();

        Collection<Long> allUsers = originalDataset.allUsers();
        Map<Long, User> usersMap = allUsers.stream().collect(Collectors.toMap(u-> u, u-> new User(u)));


        Collection<Long> allItems = originalDataset.allRatedItems();
        Map<Long, Item> itemsMap = allItems.stream().collect(Collectors.toMap(u-> u, u-> new Item(u)));

        int numTestRatings = 0;
        while (numTestRatings < 5) {
            long idUser = (Long) allUsers.toArray()[random.nextInt(allUsers.size())];
            if (!testRatings.containsKey(idUser)) {
                testRatings.put(usersMap.get(idUser), new TreeSet<>());
            }
            Set<Long> notInTrain = new TreeSet<>(originalDataset.getUserRated(idUser));
            notInTrain.removeAll(testRatings.get(idUser));
            if (notInTrain.isEmpty()) {
                allUsers.remove(idUser);
            } else {
                long idItem = (Long) notInTrain.toArray()[random.nextInt(notInTrain.size())];

                testRatings.get(idUser).add(itemsMap.get(idItem));
                numTestRatings++;
            }
        }

        TestRatingsDataset<? extends Rating> testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(originalDataset, testRatings);
        long testRatings_get = testRatingsDataset.getNumRatings();
        int testRatings_ite = 0;
        int testRatings_met = 0;
        for (Rating r : testRatingsDataset) {
            testRatings_ite++;
        }

        for (long idUser : testRatingsDataset.allUsers()) {
            for (long idItem : testRatingsDataset.getUserRated(idUser)) {
                testRatings_met++;
            }
        }

        Assert.assertEquals(testRatings_get, testRatings_met);
        Assert.assertEquals(testRatings_get, testRatings_ite);
        Assert.assertEquals(testRatings_ite, testRatings_met);
    }

    @Test
    public void testDataset() throws Exception {

        Random random = new Random(0);
        //Dataset Completo
        RatingsDataset<? extends Rating> originalDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(50, 50, 0.5);

        Map<User, Set<Item>> testRatings = new TreeMap<>();

        Collection<Long> allUsers = originalDataset.allUsers();
        Map<Long, User> usersMap = allUsers.stream().collect(Collectors.toMap(u-> u, u-> new User(u)));


        Collection<Long> allItems = originalDataset.allRatedItems();
        Map<Long, Item> itemsMap = allItems.stream().collect(Collectors.toMap(u-> u, u-> new Item(u)));

        int numTestRatings = 0;
        while (numTestRatings < 5) {
            long idUser = (Long) allUsers.toArray()[random.nextInt(allUsers.size())];
            if (!testRatings.containsKey(idUser)) {

                testRatings.put(usersMap.get(idUser), new TreeSet<>());

            }
            Set<Long> notInTrain = new TreeSet<>(originalDataset.getUserRated(idUser));
            notInTrain.removeAll(testRatings.get(idUser));
            if (notInTrain.isEmpty()) {
                allUsers.remove(idUser);
            } else {
                long idItem = (Long) notInTrain.toArray()[random.nextInt(notInTrain.size())];

                testRatings.get(idUser).add(itemsMap.get(idItem));
                numTestRatings++;
            }
        }

        TestRatingsDataset testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(originalDataset, testRatings);

        RatingsDatasetTest.testIteratorEqualsOtherMethods(testRatingsDataset);
    }
}
