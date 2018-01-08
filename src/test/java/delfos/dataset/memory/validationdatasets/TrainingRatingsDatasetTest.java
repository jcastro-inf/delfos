package delfos.dataset.memory.validationdatasets;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.TrainingRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test para probar la clase {@link TrainingRatingsDataset}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 06-Mar-2013
 */
public class TrainingRatingsDatasetTest extends DelfosTest {

    public TrainingRatingsDatasetTest() {
    }

    /**
     * Comprueba que las productos que se indican como test son visibles y el
     * resto están ocultas.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSetRating() throws Exception {

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

        Map<User, Set<Item>> testRatings = new TreeMap<>();
        Set<Item> listaItems = new TreeSet<>();
        listaItems.add(new Item(1));
        listaItems.add(new Item(2));
        testRatings.put(new User(3), listaItems);

        TrainingRatingsDataset trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(originalDataset, testRatings);

        assert trainingRatingsDataset.getRating(3, 1) == null;
        assert trainingRatingsDataset.getRating(3, 2) == null;
        assert trainingRatingsDataset.getRating(3, 3).getRatingValue().intValue() == 3;
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSize() throws Exception {
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
        BothIndexRatingsDataset<Rating> originalDataset = new BothIndexRatingsDataset(ratings);

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

        TrainingRatingsDataset<Rating> trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(originalDataset, testSet);
        Assert.assertEquals("Test set doesn't have 22 ratings", 22, trainingRatingsDataset.getNumRatings());
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testIterator() throws Exception {
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

        TrainingRatingsDataset<Rating> trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(originalDataset, testSet);
        {
            int trainingDatasetSize_accordingToIterator = 0;
            for (Rating r : trainingRatingsDataset) {
                trainingDatasetSize_accordingToIterator++;
            }
            Assert.assertEquals(22, trainingDatasetSize_accordingToIterator);
        }
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testNumberOfRatings() throws Exception {

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

        ArrayList<Rating> ratings = new ArrayList<Rating>((int) originalDataset.getNumRatings());
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

                testRatings.get(usersMap.get(idUser)).add(itemsMap.get(idItem));
                numTestRatings++;
            }
        }

        TrainingRatingsDataset<? extends Rating> trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(originalDataset, testRatings);
        long trainingRatings_get = trainingRatingsDataset.getNumRatings();
        int trainingRatings_ite = 0;
        int trainingRatings_met = 0;
        for (Rating r : trainingRatingsDataset) {
            trainingRatings_ite++;
        }

        for (long idUser : trainingRatingsDataset.allUsers()) {
            for (long idItem : trainingRatingsDataset.getUserRated(idUser)) {
                trainingRatings_met++;
            }
        }

        Assert.assertEquals(trainingRatings_get, trainingRatings_met);
        Assert.assertEquals(trainingRatings_get, trainingRatings_ite);
        Assert.assertEquals(trainingRatings_ite, trainingRatings_met);

        Assert.assertEquals(originalDataset.getNumRatings() - trainingRatingsDataset.getNumRatings(), numTestRatings);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationFailureDueToInexistingRating() throws Exception {
        DatasetLoader<Rating> datasetLoader = new MockDatasetLoader_ValidationDatasets();

        Map<User, Set<Item>> testItems = new TreeMap<>();

        long idUser = 2;
        long idItem = 13;

        testItems.put(new User(idUser), new TreeSet<>());
        testItems.get(new User(idUser)).add(new Item(idItem));

        TrainingRatingsDataset<Rating> trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), testItems);
    }
}
