package delfos.rs.explanation;

import java.util.Map;
import delfos.common.aggregationoperators.AggregationOperator;

/**
 *
 * @version 09-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class PenaltyAggregationExplanation {

    private final Map<Integer, AggregationOperator> aggregationsByItem;
    private final Map<Integer, AggregationOperator> aggregationsByItem_noTies;

    public PenaltyAggregationExplanation(Map<Integer, AggregationOperator> aggregationsByItem, Map<Integer, AggregationOperator> aggregationsByItem_noTies) {
        this.aggregationsByItem = aggregationsByItem;
        this.aggregationsByItem_noTies = aggregationsByItem_noTies;
    }

    public Map<Integer, AggregationOperator> getAggregationsByItem() {
        return aggregationsByItem;
    }

    public Map<Integer, AggregationOperator> getAggregationsByItem_noTies() {
        return aggregationsByItem_noTies;
    }

}
