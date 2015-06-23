package delfos.group.grs.consensus.itemselector;

import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class NoSelection extends GroupRecommendationsSelector {

    public NoSelection() {
        super();
    }

    @Override
    public Set<Integer> getRecommendationSelection(Map<Integer, Collection<Recommendation>> membersRecommendations) {
        Set<Integer> recommendationSelection = super.getRecommendationSelection(membersRecommendations);
        return recommendationSelection;
    }

}
