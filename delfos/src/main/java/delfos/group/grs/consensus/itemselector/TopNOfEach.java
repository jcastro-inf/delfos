package delfos.group.grs.consensus.itemselector;

import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TopNOfEach extends GroupRecommendationsSelector {

    public TopNOfEach() {
        super();

        addParameter(NUMBER_OF_ITEM_SELECTED);
    }

    @Override
    public Set<Integer> getRecommendationSelection(Map<Integer, Collection<Recommendation>> membersRecommendations) {
        super.getRecommendationSelection(membersRecommendations);

        Set<Integer> itemsSelected = new TreeSet<>();
        Map<Integer, LinkedList<Recommendation>> removableRecommendations = new TreeMap<>();
        int numItemsToSelect = getNumItemsSelect();

        membersRecommendations.entrySet().stream().forEach((entry) -> {
            LinkedList<Recommendation> sortedRemovableRecommendation = new LinkedList<>(entry.getValue());
            Collections.sort(sortedRemovableRecommendation, Recommendation.BY_PREFERENCE_DESC);
            removableRecommendations.put(entry.getKey(), sortedRemovableRecommendation);
        });

        while (itemsSelected.size() != numItemsToSelect) {
            for (int idUser : removableRecommendations.keySet()) {
                LinkedList<Recommendation> removableRecommendationsThisUser = removableRecommendations.get(idUser);

                Recommendation firstRecommendation = null;
                do {
                    firstRecommendation = removableRecommendationsThisUser.removeFirst();
                } while (!itemsSelected.add(firstRecommendation.getIdItem()));

                if (itemsSelected.size() == numItemsToSelect) {
                    break;
                }
            }
        }

        return itemsSelected;
    }

    public int getNumItemsSelect() {
        return (Integer) getParameterValue(NUMBER_OF_ITEM_SELECTED);
    }
}
