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

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.item.Item;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 02-Mayo-2014
 */
public abstract class GroupRecommendationsSelector extends ParameterOwnerAdapter {

    public static final Parameter NUMBER_OF_ITEMS_SELECTED = new Parameter(
            "NUMBER_OF_ITEM_SELECTED",
            new IntegerParameter(1, 3000, 10));

    public Set<Item> getRecommendationSelection(Collection<RecommendationsToUser> membersRecommendations) {
        checkListsIntegrity(membersRecommendations);
        return unionItemsRecommended(membersRecommendations);
    }

    private void checkListsIntegrity(Collection<RecommendationsToUser> membersRecommendations) {
        Set<Item> itemsRecommended = unionItemsRecommended(membersRecommendations);

        membersRecommendations.parallelStream().forEach(memberRecommendations -> {

            Set<Item> thisUserItemsRecommended = memberRecommendations
                    .getRecommendations().stream()
                    .map(recommendation -> recommendation.getItem())
                    .collect(Collectors.toSet());

            if (!itemsRecommended.equals(thisUserItemsRecommended)) {
                Set<Integer> genMinusMem = new TreeSet(itemsRecommended);
                genMinusMem.removeAll(thisUserItemsRecommended);

                Set<Integer> memMinusGen = new TreeSet(thisUserItemsRecommended);
                memMinusGen.removeAll(itemsRecommended);

                String message = "User " + memberRecommendations.getUser()
                        + " has a different recommendation item set.";

                throw new IllegalArgumentException(message);
            }
        });
    }

    public static Set<Item> unionItemsRecommended(Collection<RecommendationsToUser> membersRecommendations) {
        Set<Item> itemsRecommended
                = membersRecommendations.parallelStream()
                .flatMap((recommendationsToUser) -> recommendationsToUser.getRecommendations().parallelStream())
                .map(recommendation -> recommendation.getItem())
                .collect(Collectors.toSet());
        return itemsRecommended;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_RECOMMENDATION_SELECTION_MODE;
    }

}
