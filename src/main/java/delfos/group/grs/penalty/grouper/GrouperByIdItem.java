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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
