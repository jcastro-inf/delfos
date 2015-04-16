package delfos.group.grs.consensus.itemselector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import delfos.rs.recommendation.Recommendation;

public class AllItems extends GroupRecommendationsSelector {

    public AllItems() {
        super();
    }

    @Override
    public Set<Integer> getRecommendationSelection(Map<Integer, List<Recommendation>> membersRecommendations) {

        Set<Integer> itemsSelected = new TreeSet<>();

        int idUser = membersRecommendations.keySet().iterator().next();
        membersRecommendations.get(idUser).stream().forEach((r) -> {
            itemsSelected.add(r.getIdItem());
        });

        return itemsSelected;
    }
}
