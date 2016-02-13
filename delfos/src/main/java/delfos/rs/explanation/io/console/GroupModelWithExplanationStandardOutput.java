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
package delfos.rs.explanation.io.console;

import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.datastructures.histograms.HistogramCategories;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.explanation.NestedExplanation;
import delfos.rs.explanation.PenaltyAggregationExplanation;
import java.util.Map;

/**
 *
 * @version 09-sep-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupModelWithExplanationStandardOutput {

    public static void writeFile(String string, Map<GroupOfUsers, Object> groupModels) {

        HistogramCategories<AggregationOperator> generalHistogram_noTies = new HistogramCategories<>();
        HistogramCategories<AggregationOperator> generalHistogram = new HistogramCategories<>();

        for (Map.Entry<GroupOfUsers, Object> groupEntry : groupModels.entrySet()) {
            GroupOfUsers group = groupEntry.getKey();
            Object value = groupEntry.getValue();

            if (value instanceof GroupModelWithExplanation) {
                GroupModelWithExplanation groupModelWithExplanation = (GroupModelWithExplanation) value;
                PenaltyAggregationExplanation penaltyAggregationExplanation = getPenaltyAggregationExplanation(groupModelWithExplanation);
                if (penaltyAggregationExplanation == null) {
                    Global.showWarning("This doesnt have penalty explanation o");
                    return;
                }
                Map<Integer, AggregationOperator> aggregationsByItem = penaltyAggregationExplanation.getAggregationsByItem();
                Map<Integer, AggregationOperator> aggregationsByItem_noTies = penaltyAggregationExplanation.getAggregationsByItem_noTies();

                for (Map.Entry<Integer, AggregationOperator> itemEntry : aggregationsByItem.entrySet()) {
                    int idItem = itemEntry.getKey();
                    AggregationOperator aggregationOperator = itemEntry.getValue();

                    generalHistogram.addValue(aggregationOperator);
                }

                for (Map.Entry<Integer, AggregationOperator> itemEntry : aggregationsByItem_noTies.entrySet()) {
                    int idItem = itemEntry.getKey();
                    AggregationOperator aggregationOperator = itemEntry.getValue();

                    generalHistogram_noTies.addValue(aggregationOperator);
                }
            }
        }

        if (Global.isVerboseAnnoying()) {
            Global.showMessageTimestamped("Histograma sin empates");

            generalHistogram_noTies.printHistogram(System.out);

            Global.showMessageTimestamped("Histograma con empates");
            generalHistogram.printHistogram(System.out);
        }
    }

    private static PenaltyAggregationExplanation getPenaltyAggregationExplanation(Object groupModel) {
        if (groupModel instanceof GroupModelWithExplanation) {
            GroupModelWithExplanation groupModelWithExplanation = (GroupModelWithExplanation) groupModel;
            return getPenaltyAggregationExplanation(groupModelWithExplanation.getExplanation());
        }
        if (groupModel instanceof PenaltyAggregationExplanation) {
            PenaltyAggregationExplanation penaltyAggregationExplanation = (PenaltyAggregationExplanation) groupModel;

            return penaltyAggregationExplanation;
        }

        if (groupModel instanceof NestedExplanation) {
            NestedExplanation nestedExplanation = (NestedExplanation) groupModel;

            PenaltyAggregationExplanation penaltyAggregationExplanation = getPenaltyAggregationExplanation(nestedExplanation.getMyExplanation());
            if (penaltyAggregationExplanation == null) {
                penaltyAggregationExplanation = getPenaltyAggregationExplanation(nestedExplanation.getNestedExplanation());
            }

            return penaltyAggregationExplanation;
        }

        return null;
    }

}
