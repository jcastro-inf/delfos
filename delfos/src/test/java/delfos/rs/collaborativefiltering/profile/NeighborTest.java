package delfos.rs.collaborativefiltering.profile;

import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class NeighborTest {

    public NeighborTest() {
    }

    /**
     * Test of getNeighborsMap method, of class Neighbor.
     */
    @Test
    public void testSortBySimilarityDesc() {

        List<Neighbor> neighbors = Arrays.asList(
                new Neighbor(RecommendationEntity.USER, 199, Float.NaN),
                new Neighbor(RecommendationEntity.USER, 2, 0.9),
                new Neighbor(RecommendationEntity.USER, 9, 0.5),
                new Neighbor(RecommendationEntity.USER, 99, Float.NaN)
        );

        neighbors.sort(Neighbor.BY_SIMILARITY_DESC);

        assertEquals(neighbors.get(0), new Neighbor(RecommendationEntity.USER, 2, 0.9));
        assertEquals(neighbors.get(1), new Neighbor(RecommendationEntity.USER, 9, 0.5));
        assertEquals(neighbors.get(2), new Neighbor(RecommendationEntity.USER, 99, Float.NaN));
        assertEquals(neighbors.get(3), new Neighbor(RecommendationEntity.USER, 199, Float.NaN));
    }

    /**
     * Test of getNeighborsMap method, of class Neighbor.
     */
    @Test
    public void testSortBySimilarityAsc() {

        List<Neighbor> neighbors = Arrays.asList(
                new Neighbor(RecommendationEntity.USER, 199, Float.NaN),
                new Neighbor(RecommendationEntity.USER, 2, 0.9),
                new Neighbor(RecommendationEntity.USER, 9, 0.5),
                new Neighbor(RecommendationEntity.USER, 99, Float.NaN)
        );

        neighbors.sort(Neighbor.BY_SIMILARITY_ASC);

        assertEquals(neighbors.get(0), new Neighbor(RecommendationEntity.USER, 9, 0.5));
        assertEquals(neighbors.get(1), new Neighbor(RecommendationEntity.USER, 2, 0.9));
        assertEquals(neighbors.get(2), new Neighbor(RecommendationEntity.USER, 99, Float.NaN));
        assertEquals(neighbors.get(3), new Neighbor(RecommendationEntity.USER, 199, Float.NaN));
    }
}
