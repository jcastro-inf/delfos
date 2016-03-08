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
package delfos.group.grs.filtered.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.Global;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.statisticalfuncions.StandardDeviation;
import delfos.dataset.util.DatasetUtilities;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 26-ago-2014
 */
public class OutliersItemsStandardDeviationTopPercentFilter extends GroupRatingsFilter {

    private static final long serialVersionUID = 1L;

    public static final Parameter PERCENTAGE_FILTERED_OUT = new Parameter("PERCENTAGE_FILTERED_OUT", new DoubleParameter(0, 1f, 0.2f));

    private double oldPercentageFilteredOut = 0.2;

    public OutliersItemsStandardDeviationTopPercentFilter() {
        super();
        addParameter(PERCENTAGE_FILTERED_OUT);

        addParammeterListener(() -> {
            double newPercetnageFilteredOut = ((Number) getParameterValue(PERCENTAGE_FILTERED_OUT)).doubleValue();
            newPercetnageFilteredOut = NumberRounder.round(newPercetnageFilteredOut);

            if (oldPercentageFilteredOut != newPercetnageFilteredOut) {
                oldPercentageFilteredOut = newPercetnageFilteredOut;
                setAlias(OutliersItemsStandardDeviationTopPercentFilter.class.getSimpleName() + "_percentOut=" + newPercetnageFilteredOut);
            }
        });
    }

    public OutliersItemsStandardDeviationTopPercentFilter(double percentageMaxFilteredOut) {
        this();

        setParameterValue(PERCENTAGE_FILTERED_OUT, percentageMaxFilteredOut);
    }

    @Override
    public Map<Integer, Map<Integer, Number>> getFilteredRatings(Map<Integer, Map<Integer, Number>> ratingsByUser) {
        final double percentToDelete = ((Number) getParameterValue(PERCENTAGE_FILTERED_OUT)).doubleValue();

        List<ItemValuePair> itemsSortedByStandardDeviation = new ArrayList<>();

        Map<Integer, Map<Integer, Number>> ratingsByItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(ratingsByUser);

        if (ratingsByItem.size() == 1) {
            throw new IllegalArgumentException("The number of items rated by the group is 1, cannot filter items.");
        }

        for (int idItem : ratingsByItem.keySet()) {
            double standardDeviation = new StandardDeviation(ratingsByItem.get(idItem).values()).getStandardDeviation();
            itemsSortedByStandardDeviation.add(new ItemValuePair(idItem, standardDeviation));
        }

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Initial\t" + itemsSortedByStandardDeviation + "\n");
        }

        Collections.sort(itemsSortedByStandardDeviation);
        Collections.reverse(itemsSortedByStandardDeviation);

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Sorted\t" + itemsSortedByStandardDeviation + "\n");
        }

        int numItemsToDelete = (int) (itemsSortedByStandardDeviation.size() * percentToDelete);

        if (numItemsToDelete == 0) {
            numItemsToDelete = 1;
        }

        List<ItemValuePair> itemsNotFiltered = itemsSortedByStandardDeviation.subList(numItemsToDelete, itemsSortedByStandardDeviation.size());
        Map<Integer, Map<Integer, Number>> filteredRatingsByItem = new TreeMap<>();

        for (ItemValuePair itemValuePair : itemsNotFiltered) {
            int idItem = itemValuePair.idItem;
            filteredRatingsByItem.put(idItem, new TreeMap<>(ratingsByItem.get(idItem)));
        }

        Map<Integer, Map<Integer, Number>> filteredRatingsByUser
                = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(filteredRatingsByItem);
        return filteredRatingsByUser;
    }

    private class ItemValuePair implements Comparable<ItemValuePair> {

        public final int idItem;
        public final double value;

        public ItemValuePair(int idItem, double value) {
            this.idItem = idItem;
            this.value = value;
        }

        @Override
        public int compareTo(ItemValuePair o) {
            return Double.compare(this.value, o.value);
        }

        @Override
        public String toString() {
            return idItem + " -> " + Double.toString(value);
        }
    }
}
