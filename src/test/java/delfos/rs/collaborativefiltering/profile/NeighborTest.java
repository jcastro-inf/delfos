package delfos.rs.collaborativefiltering.profile;

import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
                new Neighbor(RecommendationEntity.USER, 199, Double.NaN),
                new Neighbor(RecommendationEntity.USER, 2, 0.9),
                new Neighbor(RecommendationEntity.USER, 9, 0.5),
                new Neighbor(RecommendationEntity.USER, 99, Double.NaN)
        );

        neighbors.sort(Neighbor.BY_SIMILARITY_DESC);

        assertEquals(neighbors.get(0), new Neighbor(RecommendationEntity.USER, 2, 0.9));
        assertEquals(neighbors.get(1), new Neighbor(RecommendationEntity.USER, 9, 0.5));
        assertEquals(neighbors.get(2), new Neighbor(RecommendationEntity.USER, 99, Double.NaN));
        assertEquals(neighbors.get(3), new Neighbor(RecommendationEntity.USER, 199, Double.NaN));
    }

    /**
     * Test of getNeighborsMap method, of class Neighbor.
     */
    @Test
    public void testSortBySimilarityAsc() {

        List<Neighbor> neighbors = Arrays.asList(
                new Neighbor(RecommendationEntity.USER, 199, Double.NaN),
                new Neighbor(RecommendationEntity.USER, 2, 0.9),
                new Neighbor(RecommendationEntity.USER, 9, 0.5),
                new Neighbor(RecommendationEntity.USER, 99, Double.NaN)
        );

        neighbors.sort(Neighbor.BY_SIMILARITY_ASC);

        assertEquals(neighbors.get(0), new Neighbor(RecommendationEntity.USER, 9, 0.5));
        assertEquals(neighbors.get(1), new Neighbor(RecommendationEntity.USER, 2, 0.9));
        assertEquals(neighbors.get(2), new Neighbor(RecommendationEntity.USER, 99, Double.NaN));
        assertEquals(neighbors.get(3), new Neighbor(RecommendationEntity.USER, 199, Double.NaN));
    }
}
