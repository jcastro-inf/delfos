package delfos.rs.recommendation;

import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecommendationsWithNeighbors extends Recommendations {

    private List<Neighbor> neighbors;

    public static final RecommendationsWithNeighbors EMPTY_LIST = new RecommendationsWithNeighbors(
            User.ANONYMOUS_USER.getName(),
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST);

    public RecommendationsWithNeighbors() {
        super();
    }

    public RecommendationsWithNeighbors(String targetIdentifier, Collection<Recommendation> recommendations) {
        super(targetIdentifier, recommendations);
    }

    public RecommendationsWithNeighbors(String targetIdentifier, Collection<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        super(targetIdentifier, recommendations, recommendationComputationDetails);
    }

    public RecommendationsWithNeighbors(String targetIdentifier, Collection<Recommendation> recommendations, List<Neighbor> neighbors) {
        super(targetIdentifier, recommendations);
        this.neighbors = Collections.unmodifiableList(new ArrayList<Neighbor>(neighbors));
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

}
