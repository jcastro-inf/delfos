/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.similaritymeasures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @version 15-abr-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PearsonCorrelationCoefficientTest {

    public PearsonCorrelationCoefficientTest() {
    }

    @Test
    public void testPerfectNegativeCorrelation() throws Exception {

        Double[] array1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        Double[] array2 = {5.0, 4.0, 3.0, 2.0, 1.0};

        List<Double> v1 = new ArrayList<>(Arrays.asList(array1));
        List<Double> v2 = new ArrayList<>(Arrays.asList(array2));
        PearsonCorrelationCoefficient instance = new PearsonCorrelationCoefficient();
        double expResult = -1;
        double result = instance.similarity(v1, v2);

        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testPerfectCorrelation() throws Exception {

        Double[] array1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        Double[] array2 = {1.0, 2.0, 3.0, 4.0, 5.0};

        List<Double> v1 = new ArrayList<>(Arrays.asList(array1));
        List<Double> v2 = new ArrayList<>(Arrays.asList(array2));
        PearsonCorrelationCoefficient instance = new PearsonCorrelationCoefficient();
        double expResult = 1;
        double result = instance.similarity(v1, v2);

        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testPerfectNegativeCorrelation_weighted() throws Exception {

        Double[] array1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        Double[] array2 = {5.0, 4.0, 3.0, 2.0, 1.0};
        Double[] weightsArray = {0.2, 0.2, 0.2, 0.2, 0.2};

        List<Double> v1 = new ArrayList<>(Arrays.asList(array1));
        List<Double> v2 = new ArrayList<>(Arrays.asList(array2));
        List<Double> weights = new ArrayList<>(Arrays.asList(weightsArray));

        PearsonCorrelationCoefficient instance = new PearsonCorrelationCoefficient();
        double expResult = -1;
        double result = instance.weightedSimilarity(v1, v2, weights);

        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testPerfectCorrelation_weighted() throws Exception {

        Double[] array1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        Double[] array2 = {1.0, 2.0, 3.0, 4.0, 5.0};
        Double[] weightsArray = {0.2, 0.2, 0.2, 0.2, 0.2};

        List<Double> v1 = new ArrayList<>(Arrays.asList(array1));
        List<Double> v2 = new ArrayList<>(Arrays.asList(array2));
        List<Double> weights = new ArrayList<>(Arrays.asList(weightsArray));

        PearsonCorrelationCoefficient instance = new PearsonCorrelationCoefficient();
        double expResult = 1;
        double result = instance.weightedSimilarity(v1, v2, weights);

        assertEquals(expResult, result, 0.0001);
    }

}
