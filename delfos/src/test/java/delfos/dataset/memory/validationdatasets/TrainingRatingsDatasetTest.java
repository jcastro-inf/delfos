package delfos.dataset.memory.validationdatasets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.TrainingRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;

/**
 * Test para probar la clase {@link TrainingRatingsDataset}.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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

        Map<Integer, Set<Integer>> testRatings = new TreeMap<>();
        Set<Integer> listaItems = new TreeSet<>();
        listaItems.add(1);
        listaItems.add(2);
        testRatings.put(3, listaItems);

        TrainingRatingsDataset trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(originalDataset, testRatings);

        assert trainingRatingsDataset.getRating(3, 1) == null;
        assert trainingRatingsDataset.getRating(3, 2) == null;
        assert trainingRatingsDataset.getRating(3, 3).ratingValue.intValue() == 3;
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

        Map<Integer, Set<Integer>> testSet = new TreeMap<>();
        testSet.put(1, new TreeSet<>());
        testSet.put(3, new TreeSet<>());
        testSet.get(1).add(1);
        testSet.get(1).add(5);
        testSet.get(3).add(3);

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

        Map<Integer, Set<Integer>> testSet = new TreeMap<>();
        testSet.put(1, new TreeSet<>());
        testSet.put(3, new TreeSet<>());
        testSet.get(1).add(1);
        testSet.get(1).add(5);
        testSet.get(3).add(3);

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

        TrainingRatingsDataset<? extends Rating> trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(originalDataset, testRatings);
        int trainingRatings_get = trainingRatingsDataset.getNumRatings();
        int trainingRatings_ite = 0;
        int trainingRatings_met = 0;
        for (Rating r : trainingRatingsDataset) {
            trainingRatings_ite++;
        }

        for (int idUser : trainingRatingsDataset.allUsers()) {
            for (int idItem : trainingRatingsDataset.getUserRated(idUser)) {
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

        Map<Integer, Set<Integer>> testItems = new TreeMap<>();

        int idUser = 2;
        int idItem = 13;

        testItems.put(idUser, new TreeSet<>());
        testItems.get(idUser).add(idItem);

        TrainingRatingsDataset<Rating> trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), testItems);
    }
}
