package delfos.dataset.modifieddatasets.changeratings;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.generated.modifieddatasets.changeratings.RatingsDatasetOverwrite;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class RatingsDatasetOverwriteTest {

    private static BothIndexRatingsDataset<Rating> originalRatingsDataset;

    @BeforeClass
    public static void beforeClass() {
        Collection<Rating> ratings = new LinkedList<>();

        ratings.add(new Rating(1, 11, 1));
        ratings.add(new Rating(1, 12, 1));
        ratings.add(new Rating(1, 13, 1));
        ratings.add(new Rating(1, 14, 1));
        ratings.add(new Rating(1, 15, 1));

        ratings.add(new Rating(2, 21, 2));
        ratings.add(new Rating(2, 22, 2));
        ratings.add(new Rating(2, 23, 2));
        ratings.add(new Rating(2, 24, 2));
        ratings.add(new Rating(2, 25, 2));

        originalRatingsDataset = new BothIndexRatingsDataset<>(ratings);
    }

    @Test
    public void testGetRating_newRating() throws Exception {

        int idUser = 3;
        int idItem = 1;
        int ratingValue = 5;

        Collection<Rating> newRatings = new LinkedList<>();
        newRatings.add(new Rating(idUser, idItem, ratingValue));

        RatingsDatasetOverwrite<Rating> instance = RatingsDatasetOverwrite.createRatingsDataset(originalRatingsDataset, newRatings);
        Rating expResult = new Rating(idUser, idItem, ratingValue);
        Rating result = instance.getRating(idUser, idItem);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetRating_replaceRating() throws Exception {

        int idUser = 1;
        int idItem = 12;
        int ratingValue = 5;

        Collection<Rating> newRatings = new LinkedList<>();
        newRatings.add(new Rating(idUser, idItem, ratingValue));

        RatingsDatasetOverwrite<Rating> instance = RatingsDatasetOverwrite.createRatingsDataset(originalRatingsDataset, newRatings);
        Rating expResult = new Rating(idUser, idItem, ratingValue);
        Rating result = instance.getRating(idUser, idItem);

        assertEquals(expResult, result);
    }

    @Test
    public void testAllUsers() {

        int idUser = 3;
        int idItem = 1;
        int ratingValue = 5;

        Collection<Rating> newRatings = new LinkedList<>();
        newRatings.add(new Rating(idUser, idItem, ratingValue));

        RatingsDatasetOverwrite<Rating> instance
                = RatingsDatasetOverwrite.createRatingsDataset(originalRatingsDataset, newRatings);

        Set<Integer> expectedResult = new TreeSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> result = new TreeSet<>(instance.allUsers());

        assertEquals(expectedResult, result);
    }

    @Test
    public void testAllRatedItems() {

        int idUser = 3;
        int idItem = 1;
        int ratingValue = 5;

        Collection<Rating> newRatings = new LinkedList<>();
        newRatings.add(new Rating(idUser, idItem, ratingValue));

        RatingsDatasetOverwrite<Rating> instance
                = RatingsDatasetOverwrite.createRatingsDataset(originalRatingsDataset, newRatings);

        Set<Integer> expectedResult = new TreeSet<>(Arrays.asList(
                1, 11, 12, 13, 14, 15, 21, 22, 23, 24, 25));

        Set<Integer> result = new TreeSet<>(instance.allRatedItems());

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetUserRated() throws Exception {
        int idUser = 3;
        int idItem = 1;
        int ratingValue = 5;

        Collection<Rating> newRatings = new LinkedList<>();
        newRatings.add(new Rating(idUser, idItem, ratingValue));

        RatingsDatasetOverwrite<Rating> instance = RatingsDatasetOverwrite.createRatingsDataset(originalRatingsDataset, newRatings);

        Set<Integer> result = new TreeSet<>(instance.getUserRated(idUser));
        Set<Integer> expectedResult = new TreeSet<>(Arrays.asList(1));
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetItemRated() throws Exception {
        int idUser = 3;
        int idItem = 1;
        int ratingValue = 5;

        Collection<Rating> newRatings = new LinkedList<>();
        newRatings.add(new Rating(idUser, idItem, ratingValue));

        RatingsDatasetOverwrite<Rating> instance = RatingsDatasetOverwrite.createRatingsDataset(originalRatingsDataset, newRatings);

        Set<Integer> result = new TreeSet<>(instance.getItemRated(idItem));
        Set<Integer> expectedResult = new TreeSet<>(Arrays.asList(3));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetUserRatingsRated() throws Exception {
        int idUser = 3;
        int idItem = 1;
        int ratingValue = 5;

        Collection<Rating> newRatings = new LinkedList<>();
        newRatings.add(new Rating(idUser, idItem, ratingValue));

        RatingsDatasetOverwrite<Rating> instance = RatingsDatasetOverwrite.createRatingsDataset(originalRatingsDataset, newRatings);

        Map<Integer, Rating> result = instance.getUserRatingsRated(idUser);
        Collection<Rating> expectedRatings = Arrays.asList(new Rating(3, 1, 5));
        Map<Integer, Rating> expectedResult = new BothIndexRatingsDataset<>(expectedRatings).getUserRatingsRated(idUser);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetItemRatingsRated() throws Exception {
        int idUser = 3;
        int idItem = 1;
        int ratingValue = 5;

        Collection<Rating> newRatings = new LinkedList<>();
        newRatings.add(new Rating(idUser, idItem, ratingValue));

        RatingsDatasetOverwrite<Rating> instance = RatingsDatasetOverwrite.createRatingsDataset(originalRatingsDataset, newRatings);

        Map<Integer, Rating> result = instance.getItemRatingsRated(idItem);
        Collection<Rating> expectedRatings = Arrays.asList(new Rating(3, 1, 5));
        Map<Integer, Rating> expectedResult = new BothIndexRatingsDataset<>(expectedRatings).getItemRatingsRated(idItem);

        assertEquals(expectedResult, result);
    }
}
