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
package delfos.group.grs.itemweighted.measures;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.StandardDeviation;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.util.DatasetPrinter;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Tweak2Weights extends GroupItemWeight {

    private static final long serialVersionUID = 1L;

    private static final String WEIGHTS = "WEIGHTS";
    private static final String STDEV = "STDEV";
    private static final String STDEV_PLUS = "STDEV_PLUS";
    private static final String STDEV_MINUS = "STDEV_MINUS";

    @Override
    public synchronized Map<Integer, ? extends Number> getItemWeights(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws CannotLoadRatingsDataset, UserNotFound {

        Map<Integer, Number> standardDeviations = new TreeMap<>();

        Map<Integer, Map<Integer, Number>> membersRatings = DatasetUtilities.getMembersRatings_byUser(groupOfUsers, datasetLoader);

        Map<Integer, Map<Integer, Number>> membersRatings_byItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(membersRatings);

        for (int idItem : membersRatings_byItem.keySet()) {
            Collection<Number> membersRatingsOverThisItem = membersRatings_byItem.get(idItem).values();
            StandardDeviation stdDev = new StandardDeviation(membersRatingsOverThisItem);
            double standardDeviation = stdDev.getStandardDeviation();

            double standardDeviationThisItem = standardDeviation;
            standardDeviations.put(idItem, standardDeviationThisItem);
        }
        Map<String, Map<Integer, Number>> stddev_results = new TreeMap<>();

        standardDeviationToWeights(standardDeviations, stddev_results);

        Map<Integer, Number> itemWeights = stddev_results.get(WEIGHTS);

        if (Global.isInfoPrinted()) {
            Map<Integer, Map<Integer, Number>> ratingsAndWeight = new TreeMap<>();
            ratingsAndWeight.putAll(membersRatings);
            ratingsAndWeight.put(1111, stddev_results.get(STDEV));
            ratingsAndWeight.put(2222, stddev_results.get(STDEV_MINUS));
            ratingsAndWeight.put(3333, stddev_results.get(STDEV_PLUS));
            ratingsAndWeight.put(4444, stddev_results.get(WEIGHTS));

            String ratingsAndWeight_CompactRatingTable = DatasetPrinter.printCompactRatingTable(ratingsAndWeight);
            Global.showln(ratingsAndWeight_CompactRatingTable);
            System.out.flush();
        }

        return normaliseWeights(itemWeights);
    }

    public static void standardDeviationToWeights(Map<Integer, ? extends Number> standardDeviations, Map<String, Map<Integer, Number>> results) {

        results.put(STDEV_PLUS, new TreeMap<>());
        results.put(STDEV_MINUS, new TreeMap<>());
        results.put(STDEV, new TreeMap<>());
        results.put(WEIGHTS, new TreeMap<>());

        double standardDeviationMaximum = -Double.MAX_VALUE;
        double standardDeviationMinimum = Double.MAX_VALUE;

        for (Number standardDeviation : standardDeviations.values()) {
            standardDeviationMaximum = Math.max(standardDeviationMaximum, standardDeviation.doubleValue());
            standardDeviationMinimum = Math.min(standardDeviationMinimum, standardDeviation.doubleValue());
        }

        Global.showln(Double.toString(standardDeviationMaximum));
        Global.showln(Double.toString(standardDeviationMinimum));

        for (int idItem : standardDeviations.keySet()) {

            double standardDeviation = standardDeviations.get(idItem).doubleValue();

            results.get(STDEV).put(idItem, standardDeviation);

            double standardDeviationPlus = Math.
                    sqrt(
                            Math.pow(standardDeviation, 2)
                            - Math.pow(standardDeviationMinimum, 2));

            double standardDeviationMinus = Math.
                    sqrt(
                            Math.pow(standardDeviationMaximum, 2)
                            - Math.pow(standardDeviation, 2));

            results.get(STDEV_MINUS).put(idItem, standardDeviationMinus);
            results.get(STDEV_PLUS).put(idItem, standardDeviationPlus);

            double weight = standardDeviationMinus / (standardDeviationPlus + standardDeviationMinus);

            results.get(WEIGHTS).put(idItem, weight);
        }
    }
}
