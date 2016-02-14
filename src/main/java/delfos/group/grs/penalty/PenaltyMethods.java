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
package delfos.group.grs.penalty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.dummy.AnyAggregation;
import delfos.common.aggregationoperators.penalty.functions.PenaltyFunction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.util.DatasetPrinter;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.penalty.grouper.Grouper;

/**
 *
 * @version 31-jul-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PenaltyMethods {

    protected static Map<Integer, Number> extractTrivialCasesOfAggregation(
            Map<Integer, Map<Integer, Number>> membersRatingsByItem) {

        Map<Integer, Number> trivialAggregations = new TreeMap<>();

        for (Iterator<Integer> itemsIterator = membersRatingsByItem.keySet().iterator(); itemsIterator.hasNext();) {
            int idItem = itemsIterator.next();
            Collection<Number> values = membersRatingsByItem.get(idItem).values();
            if (AnyAggregation.isTrivialCase(values)) {
                trivialAggregations.put(idItem, values.iterator().next());
                itemsIterator.remove();
            }
        }

        return trivialAggregations;
    }

    protected static Map<Integer, Number> applyAggregationVector(
            Map<Integer, Map<Integer, Number>> membersRatings,
            Map<Integer, AggregationOperator> aggregationVector) {

        if (!membersRatings.keySet().equals(aggregationVector.keySet())) {
            throw new IllegalStateException(
                    "The keys do not match! "
                    + membersRatings.keySet()
                    + " != "
                    + aggregationVector.keySet());
        }

        Map<Integer, Number> aggregatedRatings = new TreeMap<>();
        for (int idItem : membersRatings.keySet()) {
            Map<Integer, Number> ratings = membersRatings.get(idItem);
            AggregationOperator aggregationOperator = aggregationVector.get(idItem);

            double aggregatedRating = aggregationOperator.aggregateValues(ratings.values());
            aggregatedRatings.put(idItem, aggregatedRating);
        }

        return aggregatedRatings;
    }

    protected static Map<Integer, Number> aggregateDifficultAggregationCases_combinatory(
            PenaltyFunction penaltyFunction,
            GroupOfUsers groupOfUsers,
            Map<Integer, Map<Integer, Number>> membersRatingsByUser) {

        Map<Integer, Number> penaltyAggregatedRatings;

        Set<Integer> itemSet = new TreeSet<>(DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(membersRatingsByUser).keySet());

        List<Map<Integer, AggregationOperator>> allCombinationsOfAggregationVectors = getAllCombinations(itemSet, penaltyFunction.getAllowedAggregations());

        double minPenalty = Double.POSITIVE_INFINITY;
        Map<Integer, AggregationOperator> bestAggregationVector = allCombinationsOfAggregationVectors.iterator().next();

        Map<Integer, Map<Integer, Number>> membersRatingsByItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(membersRatingsByUser);
        for (Map<Integer, AggregationOperator> aggregationVector : allCombinationsOfAggregationVectors) {

            Map<Integer, Number> aggregatedValues = applyAggregationVector(membersRatingsByItem, aggregationVector);

            double penaltyValue = penaltyFunction.penalty(membersRatingsByUser, aggregatedValues);

            if (minPenalty
                    > penaltyValue) {
                minPenalty = penaltyValue;
                bestAggregationVector = aggregationVector;
            }
        }

        penaltyAggregatedRatings = applyAggregationVector(membersRatingsByItem, bestAggregationVector);

        penaltyAggregatedRatings.keySet().stream().forEach((idItem) -> {
            membersRatingsByUser.remove(idItem);
        });

        if (Global.isInfoPrinted()) {
            Global.showInfoMessage("And the best aggregation for:\n");
            String datasetTable = DatasetPrinter.printCompactRatingTable(membersRatingsByUser);

            String datasetTablePlusTabs = "\t".concat(datasetTable).replaceAll("\n", "\n\t");

            datasetTablePlusTabs = datasetTablePlusTabs.substring(0, datasetTablePlusTabs.length() - 1);

            Global.showInfoMessage(datasetTablePlusTabs);
            Global.showInfoMessage(bestAggregationVector.toString() + "\n");
            Global.showInfoMessage(penaltyAggregatedRatings.toString() + "\n");
        }

        return penaltyAggregatedRatings;
    }

    protected static void checkForFailures(Map<Integer, Map<Integer, Number>> originalValuesByItem, Map<Integer, Number> groupAggregatedProfile, Set<Integer> allCommonItemsRated, GroupOfUsers groupOfUsers) throws IllegalStateException {
        if (!originalValuesByItem.isEmpty()) {
            throw new IllegalStateException("There was an error in the computing.");
        }

        if (!groupAggregatedProfile.keySet().equals(allCommonItemsRated)) {

            TreeSet<Integer> itemsNotAggregated = new TreeSet<>(allCommonItemsRated);
            itemsNotAggregated.removeAll(groupAggregatedProfile.keySet());

            TreeSet<Integer> itemsNotInCommonSet = new TreeSet<>(groupAggregatedProfile.keySet());
            itemsNotInCommonSet.removeAll(allCommonItemsRated);

            Global.showWarning("==== Group " + groupOfUsers + " aggregation FAILED ===========\n");
            Global.showWarning("Items not aggregated: " + itemsNotAggregated + "\n\n");
            Global.showWarning("Items not in common set: " + itemsNotInCommonSet + "\n");
            Global.showWarning("==============================================================\n");

            throw new IllegalStateException("Not all items were aggregated");
        }
    }

    protected static List<Map<Integer, AggregationOperator>> getAllCombinations(Set<Integer> itemSet, List<AggregationOperator> allAggregationOperators) {
        if (itemSet.isEmpty()) {
            throw new IllegalStateException("ItemSet cannot be empty for getting the combinations");
        }

        if (allAggregationOperators.isEmpty()) {
            throw new IllegalStateException("Collection of aggregations cannot be empty for getting the combinations");
        }

        List<Map<Integer, AggregationOperator>> oldCombinations = new ArrayList<>();

        Iterator<Integer> itemIterator = itemSet.iterator();

        {
            int idItem = itemIterator.next();
            for (AggregationOperator aggregationOperator : allAggregationOperators) {
                Map<Integer, AggregationOperator> thisCombination = new TreeMap<>();
                thisCombination.put(idItem, aggregationOperator);
                oldCombinations.add(thisCombination);
            }
        }

        for (; itemIterator.hasNext();) {
            int idItem = itemIterator.next();

            List<Map<Integer, AggregationOperator>> newCombinations = new ArrayList<>();

            for (Iterator<Map<Integer, AggregationOperator>> it = oldCombinations.iterator(); it.hasNext();) {
                Map<Integer, AggregationOperator> combination = it.next();

                for (AggregationOperator aggregationOperator : allAggregationOperators) {
                    Map<Integer, AggregationOperator> combinationExtendedWithTisAggregation = new TreeMap<>(combination);
                    combinationExtendedWithTisAggregation.put(idItem, aggregationOperator);
                    newCombinations.add(combinationExtendedWithTisAggregation);
                }
            }

            oldCombinations = newCombinations;

        }

        return oldCombinations;
    }

    public static Map<Integer, Number> aggregateWithPenalty_combinatory(
            Map<Integer, Map<Integer, Number>> memberRatingsByItem, GroupOfUsers groupOfUsers, PenaltyFunction penaltyFunction, Grouper itemGrouper)
            throws IllegalStateException {

        Map<Integer, Number> groupAggregatedProfile = new TreeMap<>();
        Set<Integer> allCommonItemsRated = new TreeSet<>(memberRatingsByItem.keySet());
        Map<Integer, Number> trivialAggregations = extractTrivialCasesOfAggregation(memberRatingsByItem);
        groupAggregatedProfile.putAll(trivialAggregations);

        Map<Integer, Map<Integer, Rating>> mapOfMaps_RatingByItem = DatasetUtilities.getMapOfMaps_Rating(memberRatingsByItem);

        Collection<Collection<Integer>> itemGroups = itemGrouper.groupUsers(new BothIndexRatingsDataset<>(mapOfMaps_RatingByItem), mapOfMaps_RatingByItem.keySet());

        for (Collection<Integer> itemsThisTime : itemGroups) {
            Map<Integer, Map<Integer, Number>> thisTimeRatingsByItem = new TreeMap<>();
            itemsThisTime.stream().forEach((idItem) -> {
                thisTimeRatingsByItem.put(idItem, memberRatingsByItem.remove(idItem));
            });
            Map<Integer, Map<Integer, Number>> thisTimeRatingsByUser = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(thisTimeRatingsByItem);
            Map<Integer, Number> difficultAggregations = PenaltyMethods.aggregateDifficultAggregationCases_combinatory(penaltyFunction, groupOfUsers, thisTimeRatingsByUser);
            groupAggregatedProfile.putAll(difficultAggregations);
        }
        checkForFailures(memberRatingsByItem, groupAggregatedProfile, allCommonItemsRated, groupOfUsers);
        return groupAggregatedProfile;
    }

}
