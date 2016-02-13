package delfos.io.excel.parameterowner;

import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ParameterOwnerExcelTest {

    @Test
    public void testExtractParametersFromKnnUserBased() {
        KnnMemoryBasedNWR knnMemoryBasedNWR = new KnnMemoryBasedNWR();

        Map<String, Object> extractParameterValues = ParameterOwnerExcel.extractParameterValues(knnMemoryBasedNWR);

        for (String key : extractParameterValues.keySet()) {
            Object value = extractParameterValues.get(key);
            System.out.println(key + " => " + value);
        }

        System.out.println("");
    }

    @Test
    public void testExtractParametersFromRatingAggregationGRS() {
        AggregationOfIndividualRecommendations aggregationOfIndividualRecommendations = new AggregationOfIndividualRecommendations();

        Map<String, Object> extractParameterValues = ParameterOwnerExcel.extractParameterValues(aggregationOfIndividualRecommendations);

        for (String key : extractParameterValues.keySet()) {
            Object value = extractParameterValues.get(key);
            System.out.println(key + " => " + value);
        }

        System.out.println("");
    }

    @Test
    public void testExtractParametersFromHesitantKnnGroupUserGRS() {
        HesitantKnnGroupUser hesitantKnnGroupUser = new HesitantKnnGroupUser();

        Map<String, Object> extractParameterValues = ParameterOwnerExcel.extractParameterValues(hesitantKnnGroupUser);

        for (String key : extractParameterValues.keySet()) {
            Object value = extractParameterValues.get(key);
            System.out.println(key + " => " + value);
        }

        System.out.println("");
    }
}
