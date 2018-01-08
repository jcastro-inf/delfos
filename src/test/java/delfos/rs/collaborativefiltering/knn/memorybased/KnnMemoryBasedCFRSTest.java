package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsContent;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Assert;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jorge
 */
public class KnnMemoryBasedCFRSTest extends DelfosTest {

    private DatasetLoader<? extends Rating> datasetLoader = null;

    public KnnMemoryBasedCFRSTest() {
    }

    @Before
    public void initialiseDataset() {

        List<Rating> ratings = new ArrayList<>();

        //4 usuarios y 5 productos.
        ratings.add(new Rating(1, 11, 4));
        ratings.add(new Rating(1, 12, 4));
        ratings.add(new Rating(1, 13, 4));
        ratings.add(new Rating(1, 14, 3));

        ratings.add(new Rating(2, 11, 5));
        ratings.add(new Rating(2, 12, 5));
        ratings.add(new Rating(2, 13, 5));
        ratings.add(new Rating(2, 14, 4));

        ratings.add(new Rating(3, 11, 2));
        ratings.add(new Rating(3, 12, 2));
        ratings.add(new Rating(3, 13, 5));
        ratings.add(new Rating(3, 14, 5));

        RatingsDataset<? extends Rating> ratingsDataset = new BothIndexRatingsDataset(ratings);

        ContentDataset _contentDataset = new ContentDatasetDefault();

        Map<String, String> item1_features = new TreeMap<>();
        item1_features.put("numerical_1_numerical", "5.9");
        item1_features.put("numerical_2_numerical", "4");
        item1_features.put("nominal_1_nominal", "big");
        item1_features.put("nominal_2_nominal", "small");

        Item item1 = new Item(1, "item_1", _contentDataset.parseEntityFeatures(item1_features));

        Map<String, String> item2_features = new TreeMap<>();
        item2_features.put("numerical_1_numerical", "4");
        item2_features.put("numerical_2_numerical", "4.001");
        item2_features.put("nominal_1_nominal", "big");
        item2_features.put("nominal_2_nominal", "medium");
        Item item2 = new Item(2, "item_2", _contentDataset.parseEntityFeatures(item2_features));

        Set<Item> items = new TreeSet<>();
        items.add(item1);
        items.add(item2);
        ContentDataset contentDataset = new ContentDatasetDefault(items);
        UsersDataset usersDataset = new UsersDatasetAdapter(ratingsDataset
                .allUsers().stream()
                .map(idUser -> new User(idUser))
                .collect(Collectors.toSet()));

        datasetLoader = new DatasetLoaderGivenRatingsContent(
                ratingsDataset,
                contentDataset,
                usersDataset
        );

    }

    @Test
    public void testBasicWithPearson() throws CannotLoadRatingsDataset, UserNotFound {

        KnnMemoryBasedCFRS knnMemoryBasedCFRS = new KnnMemoryBasedCFRS();

        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.SIMILARITY_MEASURE, new PearsonCorrelationCoefficient());
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.RELEVANCE_FACTOR, 30);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING_VALUE, null);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING, false);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.CASE_AMPLIFICATION, 1);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE, 20);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE, new WeightedSum());

        UsersDataset usersDataset = ((UsersDatasetLoader) datasetLoader).getUsersDataset();

        User user = usersDataset.getUser(1);
        User neighborUser = usersDataset.getUser(2);

        List<Neighbor> neighbors = KnnMemoryBasedCFRS.getNeighbors(datasetLoader, user, knnMemoryBasedCFRS);
        assertArrayEquals(
                "The neighbor list is wrong",
                Arrays.asList(
                        new Neighbor(RecommendationEntity.USER, neighborUser, 1),
                        new Neighbor(RecommendationEntity.USER, 3, -0.57735026)).toArray(),
                neighbors.toArray());

    }

    @Test
    public void testBasicWithCosine() throws CannotLoadRatingsDataset, UserNotFound {
        KnnMemoryBasedCFRS knnMemoryBasedCFRS = new KnnMemoryBasedCFRS();

        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.SIMILARITY_MEASURE, new PearsonCorrelationCoefficient());
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.RELEVANCE_FACTOR, 30);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING_VALUE, null);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING, false);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.CASE_AMPLIFICATION, 1);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE, 20);
        knnMemoryBasedCFRS.setParameterValue(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE, new WeightedSum());

        UsersDataset usersDataset = ((UsersDatasetLoader) datasetLoader).getUsersDataset();

        User user = usersDataset.getUser(1);
        User neighborUser2 = usersDataset.getUser(2);
        User neighborUser3 = usersDataset.getUser(3);

        List<Neighbor> neighbors = KnnMemoryBasedCFRS.getNeighbors(datasetLoader, user, knnMemoryBasedCFRS);

        assertArrayEquals(
                "The neighbor list is wrong",
                Arrays.asList(
                        new Neighbor(RecommendationEntity.USER, neighborUser2, 0.9997108),
                        new Neighbor(RecommendationEntity.USER, neighborUser3, 0.886990057)).toArray(),
                neighbors.toArray());
    }

    @Test
    public void testGetNeighborsMethodReturnsANeighborForEachUserButHimself() {

        UsersDataset usersDataset = ((UsersDatasetLoader) datasetLoader).getUsersDataset();

        KnnMemoryBasedCFRS knnMemoryBasedCFRS = new KnnMemoryBasedCFRS();

        boolean requirementViolated = usersDataset.stream().anyMatch(user -> {

            List<Neighbor> neighbors = KnnMemoryBasedCFRS.getNeighbors(datasetLoader, user, knnMemoryBasedCFRS);

            Set<Long> allUsers = usersDataset.parallelStream()
                    .map(itemInner -> itemInner.getId())
                    .filter(innerItem -> !innerItem.equals(user.getId()))
                    .collect(Collectors.toCollection(TreeSet::new));

            Set<Long> itemsSimilares = neighbors.parallelStream()
                    .map(neighbor -> neighbor.getIdNeighbor())
                    .collect(Collectors.toCollection(TreeSet::new));

            return !allUsers.equals(itemsSimilares);
        });

        Assert.assertFalse(
                "The method does not fulfills its requirements, "
                + "a neighbor item should be returned for each item.",
                requirementViolated);
    }

}
