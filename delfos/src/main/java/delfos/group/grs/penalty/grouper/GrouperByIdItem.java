package delfos.group.grs.penalty.grouper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 *
 * @version 18-sep-2014
* @author Jorge Castro Gallardo
 */
public class GrouperByIdItem extends Grouper {

    public static final Parameter NUM_ITEMS = new Parameter("NUM_ITEMS", new IntegerParameter(2, 1000, 3));

    private int oldNumItems = 3;

    public GrouperByIdItem() {
        super();
        addParameter(NUM_ITEMS);

        addParammeterListener(() -> {
            int newNumItems = (Integer) getParameterValue(NUM_ITEMS);

            String oldParametersAlias = this.getClass().getSimpleName()
                    + "_" + oldNumItems;
            String newParametersAlias = this.getClass().getSimpleName()
                    + "_" + newNumItems;

            if (!oldParametersAlias.equals(newParametersAlias)) {
                oldNumItems = newNumItems;
                setAlias(newParametersAlias);
            }

        });
    }

    public GrouperByIdItem(int numItems) {
        this();
        setParameterValue(NUM_ITEMS, numItems);
    }

    @Override
    public Collection<Collection<Integer>> groupUsers(RatingsDataset<? extends Rating> ratings, Set<Integer> users) {

        final int numItems = (Integer) getParameterValue(NUM_ITEMS);

        Collection<Collection<Integer>> itemsSplitted = new ArrayList<>();
        Set<Integer> itemsToSplit = new TreeSet<>(users);

        while (!itemsToSplit.isEmpty()) {

            ArrayList<Integer> itemsThisTime = new ArrayList<>();
            for (Iterator<Integer> it = itemsToSplit.iterator(); it.hasNext();) {
                int idItem = it.next();

                it.remove();
                itemsThisTime.add(idItem);
                if (itemsThisTime.size() == numItems) {
                    break;
                }
            }

            itemsSplitted.add(itemsThisTime);
        }

        return itemsSplitted;
    }
}
