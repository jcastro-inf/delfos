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
import delfos.dataset.basic.item.Item;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BordaCount extends GroupRecommendationsSelector {

    public BordaCount() {
        super();

        addParameter(NUMBER_OF_ITEMS_SELECTED);
    }

    @Override
    public Set<Item> getRecommendationSelection(Collection<RecommendationsToUser> membersRecommendations) {
        super.getRecommendationSelection(membersRecommendations);

        long numItems = getNumItemsSelect();
        Set<Item> itemsSelected = new TreeSet<>();

        Map<Item, Integer> bordaCount = Collections.synchronizedMap(new TreeMap<>());

        membersRecommendations.stream().forEach(recommendationsToMember -> {
            List<Recommendation> recommendationsReverse
                    = recommendationsToMember.getRecommendations().stream()
                    .filter(Recommendation.NON_COVERAGE_FAILURES)
                    .sorted(Recommendation.BY_PREFERENCE_DESC)
                    .collect(Collectors.toList());

            IntStream.range(0, recommendationsReverse.size()).boxed().forEach(index -> {
                Item itemRecommended = recommendationsReverse.get(index).getItem();
                int bordaValue = index + 1;
                synchronized (bordaCount) {
                    Integer itemBordaValue = bordaValue;
                    if (bordaCount.containsKey(itemRecommended)) {
                        itemBordaValue += bordaCount.get(itemRecommended);
                    }
                    bordaCount.put(itemRecommended, itemBordaValue);
                }
            });
        });

        PriorityQueue<PriorityItem<Item>> queue = new PriorityQueue<>();

        bordaCount.entrySet().stream().forEach((entry) -> {
            queue.add(new PriorityItem<>(entry.getKey(), entry.getValue()));
        });

        final int initialSize = queue.size();
        for (int i = 0; i < Math.min(numItems, initialSize); i++) {
            PriorityItem<Item> item = queue.poll();
            itemsSelected.add(item.getKey());
        }

        return itemsSelected;
    }

    public int getNumItemsSelect() {
        return (Integer) getParameterValue(NUMBER_OF_ITEMS_SELECTED);
    }
}
