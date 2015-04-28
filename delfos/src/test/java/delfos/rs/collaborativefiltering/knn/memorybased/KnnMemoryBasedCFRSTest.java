package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsContent;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    public void initialiseDataset() throws ItemAlreadyExists {

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

        DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset);
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

        Collection<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        ContentDataset contentDataset = new ContentDatasetDefault(items);

        datasetLoader = new DatasetLoaderGivenRatingsContent(
                ratingsDataset,
                contentDataset);

        DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset);
    }

    @Test
    public void testBasicWithPearson() throws CannotLoadRatingsDataset, UserNotFound {
        KnnMemoryBasedCFRS knnMemory = new KnnMemoryBasedCFRS(new PearsonCorrelationCoefficient(), null, null, false, 1, 20, new WeightedSum());

        List<Neighbor> neighbors = knnMemory.getNeighbors(datasetLoader.getRatingsDataset(), 1);
        assertArrayEquals(
                "The neighbor list is wrong",
                Arrays.asList(
                        new Neighbor(RecommendationEntity.USER, 2, 1)).toArray(),
                neighbors.toArray());

    }

    @Test
    public void testBasicWithCosine() throws CannotLoadRatingsDataset, UserNotFound {
        KnnMemoryBasedCFRS knnMemory = new KnnMemoryBasedCFRS(new CosineCoefficient(), null, null, false, 1, 20, new WeightedSum());

        List<Neighbor> neighbors = knnMemory.getNeighbors(datasetLoader.getRatingsDataset(), 1);

        assertArrayEquals(
                "The neighbor list is wrong",
                Arrays.asList(
                        new Neighbor(RecommendationEntity.USER, 2, 0.9997108),
                        new Neighbor(RecommendationEntity.USER, 3, 0.886990057)).toArray(),
                neighbors.toArray());
    }
}
