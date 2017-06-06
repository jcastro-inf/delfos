package delfos.dataset.memory.validationdatasets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.TestRatingsDataset;
import delfos.dataset.storage.validationdatasets.TrainingRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 06-Mar-2013
 */
public class ValidationDatasetsTest extends DelfosTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     * @throws UserNotFound
     * @throws ItemNotFound
     */
    @Test
    public void testIntersection() throws UserNotFound, ItemNotFound {
        //Dataset Completo
        int numUsers = 5;
        int numItems = 5;
        Number rating = 3;

        List<Rating> ratings = new ArrayList<Rating>(numUsers * numItems);
        for (int idUser = 1; idUser <= numUsers; idUser++) {
            for (int idItem = 1; idItem <= numItems; idItem++) {
                ratings.add(new Rating(idUser, idItem, rating));
            }
        }
        BothIndexRatingsDataset<Rating> originalDataset = new BothIndexRatingsDataset<Rating>(ratings);

        Map<Long, Set<Long>> testItems = new TreeMap<Long, Set<Long>>();
        for (long idUser = 1; idUser <= 5; idUser++) {
            testItems.put(idUser, new TreeSet<Long>());
            testItems.get(idUser).add(6 - idUser);
        }

        TrainingRatingsDataset<Rating> trainingRatingsDataset = ValidationDatasets.getInstance().createTrainingDataset(originalDataset, testItems);
        TestRatingsDataset<Rating> testRatingsDataset = ValidationDatasets.getInstance().createTestDataset(originalDataset, testItems);

        //Compruebo que el training no está en el test
        for (Rating r : trainingRatingsDataset) {
            Assert.assertEquals("A rating of train set is in test set", null, testRatingsDataset.getRating(r.getIdUser(), r.getIdItem()));
        }

        //Compruebo que el test no está en el training
        for (Rating r : testRatingsDataset) {
            Assert.assertEquals("A rating of test set is in train set", null, trainingRatingsDataset.getRating(r.getIdUser(), r.getIdItem()));
        }

        //Compruebo que todas las valoraciones están en alguno de los dos
        for (Rating r : originalDataset) {
            boolean estaEnTest = testRatingsDataset.getRating(r.getIdUser(), r.getIdItem()) != null;
            boolean estaEnTrain = trainingRatingsDataset.getRating(r.getIdUser(), r.getIdItem()) != null;

            //Está en training y en test, error
            assert !(estaEnTest && estaEnTrain);

            //No está en training ni en test, error
            assert estaEnTest || estaEnTrain;

            //Compruebo que está en uno de los dos
            assert (estaEnTest && !estaEnTrain) || (!estaEnTest && estaEnTrain);
        }
    }
}
