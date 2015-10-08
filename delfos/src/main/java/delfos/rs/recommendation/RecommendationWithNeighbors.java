package delfos.rs.recommendation;

import delfos.dataset.basic.item.Item;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommendationWithNeighbors extends Recommendation {

    private final List<Neighbor> neighbors;

    public RecommendationWithNeighbors(Integer idItem, Number preference, List<Neighbor> neighbors) {
        super(idItem, preference);
        this.neighbors = Collections.unmodifiableList(new ArrayList<Neighbor>(neighbors));
    }

    public RecommendationWithNeighbors(Item item, Number preference, List<Neighbor> neighbors) {
        super(item, preference);
        this.neighbors = Collections.unmodifiableList(new ArrayList<Neighbor>(neighbors));
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

}
