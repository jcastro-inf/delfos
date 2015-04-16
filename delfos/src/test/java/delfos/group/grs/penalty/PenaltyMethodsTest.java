/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.group.grs.penalty;

import delfos.group.grs.penalty.PenaltyMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.Median;
import delfos.common.aggregationoperators.RMSMean;

/**
 *
 * @version 12-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class PenaltyMethodsTest {

    public PenaltyMethodsTest() {
    }

    @Test
    public void testGetAllCombinations() {
        Set<Integer> itemSet = new TreeSet<>();
        itemSet.add(15);
        itemSet.add(23);
        itemSet.add(42);

        List<AggregationOperator> allAggregationOperators = new ArrayList<>();
        allAggregationOperators.add(new Mean());
        allAggregationOperators.add(new Median());
        allAggregationOperators.add(new RMSMean());

        List<Map<Integer, AggregationOperator>> allCombinations = PenaltyMethods.getAllCombinations(itemSet, allAggregationOperators);

        for (Map<Integer, AggregationOperator> combination : allCombinations) {
            System.out.println(combination);
        }
    }

}
