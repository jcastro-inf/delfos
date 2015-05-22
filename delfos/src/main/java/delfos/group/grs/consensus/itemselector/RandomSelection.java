package delfos.group.grs.consensus.itemselector;

import delfos.experiment.SeedHolder;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class RandomSelection extends GroupRecommendationsSelector implements SeedHolder {

    public RandomSelection() {
        super();

        addParameter(NUMBER_OF_ITEM_SELECTED);
        addParameter(SEED);
    }

    @Override
    public Set<Integer> getRecommendationSelection(Map<Integer, Collection<Recommendation>> membersRecommendations) {
        Set<Integer> itemsToSelect = super.getRecommendationSelection(membersRecommendations);

        long groupSeed = getGroupSeed(membersRecommendations.keySet());
        long numItems = getNumItemsSelect();
        Random random = new Random(groupSeed);

        Set<Integer> itemsSelected = new TreeSet<>();

        while (itemsSelected.size() < numItems) {
            int nextRandom = random.nextInt(itemsToSelect.size());
            int idItem = itemsToSelect.toArray(new Integer[0])[nextRandom];
            itemsSelected.add(idItem);
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

    private long getGroupSeed(Set<Integer> keySet) {
        long seedValue = getSeedValue();

        for (int idUser : keySet) {
            seedValue += idUser;
        }
        return seedValue;
    }

    public int getNumItemsSelect() {
        return (Integer) getParameterValue(NUMBER_OF_ITEM_SELECTED);
    }
}
