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

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.user.User;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
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

        addParameter(NUMBER_OF_ITEMS_SELECTED);
    }

    @Override
    public Set<Item> getRecommendationSelection(Collection<RecommendationsToUser> membersRecommendations) {
        super.getRecommendationSelection(membersRecommendations);

        Set<Item> itemsSelected = new TreeSet<>();
        Map<User, LinkedList<Recommendation>> removableRecommendations = new TreeMap<>();
        int numItemsToSelect = getNumItemsSelect();

        membersRecommendations.stream().forEach((entry) -> {
            User member = entry.getUser();
            LinkedList<Recommendation> sortedRemovableRecommendation = new LinkedList<>(entry.getRecommendations());
            Collections.sort(sortedRemovableRecommendation, Recommendation.BY_PREFERENCE_DESC);
            removableRecommendations.put(member, sortedRemovableRecommendation);
        });

        while (itemsSelected.size() != numItemsToSelect) {
            for (User idUser : removableRecommendations.keySet()) {
                LinkedList<Recommendation> removableRecommendationsThisUser = removableRecommendations.get(idUser);

                Recommendation firstRecommendation = null;
                do {
                    firstRecommendation = removableRecommendationsThisUser.removeFirst();
                } while (!itemsSelected.add(firstRecommendation.getItem()));

                if (itemsSelected.size() == numItemsToSelect) {
                    break;
                }
            }
        }

        return itemsSelected;
    }

    public int getNumItemsSelect() {
        return (Integer) getParameterValue(NUMBER_OF_ITEMS_SELECTED);
    }
}
