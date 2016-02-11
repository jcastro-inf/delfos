/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.group.grs.consensus.itemselector;

import delfos.common.datastructures.queue.PriorityItem;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class BordaCount extends GroupRecommendationsSelector {

    public BordaCount() {
        super();

        addParameter(NUMBER_OF_ITEM_SELECTED);
    }

    @Override
    public Set<Integer> getRecommendationSelection(Map<Integer, Collection<Recommendation>> membersRecommendations) {
        super.getRecommendationSelection(membersRecommendations);

        long numItems = getNumItemsSelect();
        Set<Integer> itemsSelected = new TreeSet<>();

        Map<Integer, Integer> bordaCount = new TreeMap<>();

        for (int idUser : membersRecommendations.keySet()) {
            List<Recommendation> reverseList = new ArrayList<>(membersRecommendations.get(idUser));
            Collections.sort(reverseList, Recommendation.BY_PREFERENCE_DESC);
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
