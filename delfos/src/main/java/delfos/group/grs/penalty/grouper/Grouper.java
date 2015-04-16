package delfos.group.grs.penalty.grouper;

import java.util.Collection;
import java.util.Set;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 *
 * @version 18-sep-2014
* @author Jorge Castro Gallardo
 */
public abstract class Grouper extends ParameterOwnerAdapter {

    public abstract Collection<Collection<Integer>> groupUsers(
            RatingsDataset<? extends Rating> ratings,
            Set<Integer> users);

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUPER;
    }
}
