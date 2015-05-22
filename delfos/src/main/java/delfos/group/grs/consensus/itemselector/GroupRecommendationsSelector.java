package delfos.group.grs.consensus.itemselector;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Jorge Castro Gallardo
 *
 * @version 02-Mayo-2014
 */
public abstract class GroupRecommendationsSelector extends ParameterOwnerAdapter {

    public static final Parameter NUMBER_OF_ITEM_SELECTED = new Parameter(
            "NUMBER_OF_ITEM_SELECTED",
            new IntegerParameter(1, 3000, 10));

    public GroupRecommendationsSelector() {
    }

    public GroupRecommendationsSelector(int numberOfItems) {
        this();

        setParameterValue(NUMBER_OF_ITEM_SELECTED, numberOfItems);
    }

    public Set<Integer> getRecommendationSelection(Map<Integer, Collection<Recommendation>> membersRecommendations) {
        checkListsIntegrity(membersRecommendations);
        return getItemsRecommended(membersRecommendations);
    }

    private void checkListsIntegrity(Map<Integer, Collection<Recommendation>> membersRecommendations) {
        Set<Integer> itemsRecommended = getItemsRecommended(membersRecommendations);

        for (int idMember : membersRecommendations.keySet()) {
            Set<Integer> thisUserItemsRecommended = new TreeSet<>();
            membersRecommendations.get(idMember).stream().forEach((r) -> {
                thisUserItemsRecommended.add(r.getIdItem());
            });
            if (!itemsRecommended.equals(thisUserItemsRecommended)) {
                Set<Integer> genMinusMem = new TreeSet(itemsRecommended);
                genMinusMem.removeAll(thisUserItemsRecommended);

                Set<Integer> memMinusGen = new TreeSet(thisUserItemsRecommended);
                memMinusGen.removeAll(itemsRecommended);

                throw new IllegalArgumentException("Recommendation list for member '" + idMember + "' is not the same (group '" + membersRecommendations.keySet().toString() + "')");
            }
        }
    }

    private Set<Integer> getItemsRecommended(Map<Integer, Collection<Recommendation>> membersRecommendations) {
        Set<Integer> itemsRecommended = new TreeSet<>();
        membersRecommendations.keySet().stream().forEach((idMember) -> {
            membersRecommendations.get(idMember).stream().forEach((recommendation) -> {
                itemsRecommended.add(recommendation.getIdItem());
            });
        });

        return itemsRecommended;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_RECOMMENDATION_SELECTION_MODE;
    }

}
