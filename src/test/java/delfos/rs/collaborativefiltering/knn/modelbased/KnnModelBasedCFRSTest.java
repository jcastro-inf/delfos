package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.user.User;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.recommendationcandidates.AllCatalogItems;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class KnnModelBasedCFRSTest {

    private final RandomDatasetLoader datasetLoader;

    public KnnModelBasedCFRSTest() {
        datasetLoader = new RandomDatasetLoader(25, 25, 0.5);
        datasetLoader.setSeedValue(123456);
    }

    /**
     * Test of buildRecommendationModel method, of class KnnModelBasedCFRS.
     */
    @Test
    public void testBuildRecommendationModel() {
        KnnModelBasedCFRS instance = new KnnModelBasedCFRS();
        KnnModelBasedCFRSModel model = instance.buildRecommendationModel(datasetLoader);
    }

    /**
     * Test of recommendToUser method, of class KnnModelBasedCFRS.
     */
    @Test
    public void testRecommendToUser() {
        KnnModelBasedCFRS instance = new KnnModelBasedCFRS();
        KnnModelBasedCFRSModel model = instance.buildRecommendationModel(datasetLoader);

        User user = datasetLoader.getUsersDataset().get(1);
        Set<Item> candidateItems = new AllCatalogItems().candidateItems(datasetLoader, user);

        Collection<Recommendation> result = instance
                .recommendToUser(datasetLoader, model, user.getId(),
                        candidateItems.stream().map(item -> item.getId()).collect(Collectors.toSet())
                );
    }

    @Test
    public void testGetNeighborsMethodReturnsANeighborForEachItemButHimself() {

        ContentDataset contentDataset = datasetLoader.getContentDataset();

        KnnModelBasedCFRS instance = new KnnModelBasedCFRS();

        boolean requirementViolated = contentDataset.stream().anyMatch(item -> {

            List<Neighbor> neighbors = instance.getNeighbors(datasetLoader, item);

            Set<Integer> allItems = contentDataset.parallelStream()
                    .map(itemInner -> itemInner.getId())
                    .filter(innerItem -> !innerItem.equals(item.getId()))
                    .collect(Collectors.toCollection(TreeSet::new));

            Set<Integer> itemsSimilares = neighbors.parallelStream()
                    .map(neighbor -> neighbor.getIdNeighbor())
                    .collect(Collectors.toCollection(TreeSet::new));

            return !allItems.equals(itemsSimilares);
        });

        Assert.assertFalse(
                "The method does not fulfills its requirements, "
                + "a neighbor item should be returned for each item.",
                requirementViolated);
    }
}
