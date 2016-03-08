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
import delfos.experiment.SeedHolder;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class RandomSelection extends GroupRecommendationsSelector implements SeedHolder {

    public RandomSelection() {
        super();

        addParameter(NUMBER_OF_ITEMS_SELECTED);
        addParameter(SEED);
    }

    @Override
    public Set<Item> getRecommendationSelection(Collection<RecommendationsToUser> membersRecommendations) {
        List<Item> itemsToSelect = membersRecommendations.stream().
                flatMap(memberRecommendations -> memberRecommendations.getRecommendations().stream())
                .map(recommendation -> recommendation.getItem())
                .distinct().collect(Collectors.toList());

        Set<User> members = membersRecommendations.stream()
                .map(memberRecommendation -> memberRecommendation.getUser())
                .collect(Collectors.toSet());

        long groupSeed = getGroupSeed(members);
        long numItems = getNumItemsSelect();
        Random random = new Random(groupSeed);

        Set<Item> itemsSelected = new TreeSet<>();

        while (itemsSelected.size() < numItems) {
            int nextRandom = random.nextInt(itemsToSelect.size());
            Item item = itemsToSelect.remove(nextRandom);
            itemsSelected.add(item);
        }

        itemsSelected = Collections.unmodifiableSet(itemsSelected);

        return itemsSelected;
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    private long getGroupSeed(Set<User> users) {
        long seedValue = getSeedValue();

        int userHashSum = users.stream()
                .mapToInt(user -> user.getId().hashCode())
                .sum();

        seedValue += userHashSum;
        return seedValue;
    }

    public int getNumItemsSelect() {
        return (Integer) getParameterValue(NUMBER_OF_ITEMS_SELECTED);
    }
}
