package delfos.group.grs.filtered.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.Global;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.statisticalfuncions.StandardDeviation;
import delfos.dataset.util.DatasetUtilities;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 26-ago-2014
 */
public class OutliersItemsStandardDeviationThresholdFilter extends GroupRatingsFilter {

    private static final long serialVersionUID = 1L;

    public static final Parameter THRESHOLD = new Parameter("THRESHOLD", new FloatParameter(0, 1000f, 0.8f));

    private double oldThreshold = 0.8;

    public OutliersItemsStandardDeviationThresholdFilter() {
        super();
        addParameter(THRESHOLD);

        addParammeterListener(() -> {
            double newThreshold = ((Number) getParameterValue(THRESHOLD)).doubleValue();
            newThreshold = NumberRounder.round(newThreshold, 2);

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
    public Map<Integer, Map<Integer, Number>> getFilteredRatings(Map<Integer, Map<Integer, Number>> ratingsByUser) {
        final double threshold = ((Number) getParameterValue(THRESHOLD)).doubleValue();

        List<ItemValuePair> itemsSortedByStandardDeviation = new ArrayList<>();

        Map<Integer, Map<Integer, Number>> ratingsByItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(ratingsByUser);

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

        List<Integer> itemsRemaining = new ArrayList<>();

        itemsSortedByStandardDeviation.stream().filter((itemValuePair) -> (itemValuePair.value <= threshold)).forEach((itemValuePair) -> {
            itemsRemaining.add(itemValuePair.idItem);
        });

        Map<Integer, Map<Integer, Number>> filteredRatingsByItem = new TreeMap<>();

        itemsRemaining.stream().forEach((idItem) -> {
            filteredRatingsByItem.put(idItem, new TreeMap<>(ratingsByItem.get(idItem)));
        });

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
