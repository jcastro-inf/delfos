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
public class OutliersItemsStandardDeviationThresholdFilter extends GroupRatingsFilter {

    private static final long serialVersionUID = 1L;

    public static final Parameter THRESHOLD = new Parameter("THRESHOLD", new DoubleParameter(0, 1000f, 0.8f));

    private double oldThreshold = 0.8;

    public OutliersItemsStandardDeviationThresholdFilter() {
        super();
        addParameter(THRESHOLD);

        addParammeterListener(() -> {
            double newThreshold = ((Number) getParameterValue(THRESHOLD)).doubleValue();
            newThreshold = NumberRounder.round(newThreshold);

            if (oldThreshold != newThreshold) {
                oldThreshold = newThreshold;
                setAlias(OutliersItemsStandardDeviationThresholdFilter.class.getSimpleName() + "_u=" + newThreshold);
            }
        });
    }

    public OutliersItemsStandardDeviationThresholdFilter(double threshold) {
        this();
        setParameterValue(THRESHOLD, threshold);
    }

    @Override
    public Map<Long, Map<Long, Number>> getFilteredRatings(Map<Long, Map<Long, Number>> ratingsByUser) {
        final double threshold = ((Number) getParameterValue(THRESHOLD)).doubleValue();

        List<ItemValuePair> itemsSortedByStandardDeviation = new ArrayList<>();

        Map<Long, Map<Long, Number>> ratingsByItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(ratingsByUser);

        if (ratingsByItem.size() == 1) {
            throw new IllegalArgumentException("The number of items rated by the group is 1, cannot filter items.");
        }

        ratingsByItem.keySet().stream().forEach((idItem) -> {
            double standardDeviation = new StandardDeviation(ratingsByItem.get(idItem).values()).getStandardDeviation();
            itemsSortedByStandardDeviation.add(new ItemValuePair(idItem, standardDeviation));
        });

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Initial\t" + itemsSortedByStandardDeviation + "\n");
        }

        Collections.sort(itemsSortedByStandardDeviation);
        Collections.reverse(itemsSortedByStandardDeviation);

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Sorted\t" + itemsSortedByStandardDeviation + "\n");
        }

        List<Long> itemsRemaining = new ArrayList<>();

        itemsSortedByStandardDeviation.stream().filter((itemValuePair) -> (itemValuePair.value <= threshold)).forEach((itemValuePair) -> {
            itemsRemaining.add(itemValuePair.idItem);
        });

        Map<Long, Map<Long, Number>> filteredRatingsByItem = new TreeMap<>();

        itemsRemaining.stream().forEach((idItem) -> {
            filteredRatingsByItem.put(idItem, new TreeMap<>(ratingsByItem.get(idItem)));
        });

        Map<Long, Map<Long, Number>> filteredRatingsByUser
                = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(filteredRatingsByItem);
        return filteredRatingsByUser;
    }

    private class ItemValuePair implements Comparable<ItemValuePair> {

        public final long idItem;
        public final double value;

        public ItemValuePair(long idItem, double value) {
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
