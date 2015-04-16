package delfos.group.grs.consensus.itemselector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.rs.recommendation.Recommendation;

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

    public abstract Set<Integer> getRecommendationSelection(Map<Integer, List<Recommendation>> membersRecommendations);

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_RECOMMENDATION_SELECTION_MODE;
    }

}
