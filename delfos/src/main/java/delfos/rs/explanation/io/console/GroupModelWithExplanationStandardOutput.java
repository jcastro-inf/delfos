package delfos.rs.explanation.io.console;

import java.util.Map;
import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.datastructures.histograms.HistogramCategories;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.explanation.NestedExplanation;
import delfos.rs.explanation.PenaltyAggregationExplanation;

/**
 *
 * @version 09-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
            System.out.println("Histograma sin empates");

            generalHistogram_noTies.printHistogram(System.out);

            System.out.println("Histograma con empates");
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
