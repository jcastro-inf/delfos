package delfos.group.grs.consensus.itemselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.datastructures.queue.PriorityItem;
import delfos.rs.recommendation.Recommendation;

public class BordaCount extends GroupRecommendationsSelector {

    public BordaCount() {
        super();

        addParameter(NUMBER_OF_ITEM_SELECTED);
    }

    @Override
    public Set<Integer> getRecommendationSelection(Map<Integer, List<Recommendation>> membersRecommendations) {

        long numItems = getNumItemsSelect();
        Set<Integer> itemsSelected = new TreeSet<>();

        Map<Integer, Integer> bordaCount = new TreeMap<>();

        for (int idUser : membersRecommendations.keySet()) {
            ArrayList<Recommendation> reverseList = new ArrayList<>(membersRecommendations.get(idUser));
            Collections.reverse(reverseList);

            int index = 1;
            for (Recommendation r : reverseList) {
                int idItem = r.getIdItem();
                if (!bordaCount.containsKey(idItem)) {
                    bordaCount.put(idItem, 0);
                }
                int value = bordaCount.get(idItem);
                bordaCount.put(idItem, value + index);
                index++;
            }
        }

        PriorityQueue<PriorityItem<Integer>> queue = new PriorityQueue<>();

        bordaCount.entrySet().stream().forEach((entry) -> {
            queue.add(new PriorityItem<>(entry.getKey(), entry.getValue()));
        });

        final int initialSize = queue.size();
        for (int i = 0; i < Math.min(numItems, initialSize); i++) {
            PriorityItem<Integer> item = queue.poll();
            itemsSelected.add(item.getKey());
        }

        return itemsSelected;
    }

    public int getNumItemsSelect() {
        return (Integer) getParameterValue(NUMBER_OF_ITEM_SELECTED);
    }
}
