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

        Float[] array1 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        Float[] array2 = {5.0f, 4.0f, 3.0f, 2.0f, 1.0f};

        List<Float> v1 = new ArrayList<>(Arrays.asList(array1));
        List<Float> v2 = new ArrayList<>(Arrays.asList(array2));
        PearsonCorrelationCoefficient instance = new PearsonCorrelationCoefficient();
        float expResult = -1;
        float result = instance.similarity(v1, v2);

        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testPerfectCorrelation() throws Exception {

        Float[] array1 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        Float[] array2 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};

        List<Float> v1 = new ArrayList<>(Arrays.asList(array1));
        List<Float> v2 = new ArrayList<>(Arrays.asList(array2));
        PearsonCorrelationCoefficient instance = new PearsonCorrelationCoefficient();
        float expResult = 1;
        float result = instance.similarity(v1, v2);

        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testPerfectNegativeCorrelation_weighted() throws Exception {

        Float[] array1 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        Float[] array2 = {5.0f, 4.0f, 3.0f, 2.0f, 1.0f};
        Float[] weightsArray = {0.2f, 0.2f, 0.2f, 0.2f, 0.2f};

        List<Float> v1 = new ArrayList<>(Arrays.asList(array1));
        List<Float> v2 = new ArrayList<>(Arrays.asList(array2));
        List<Float> weights = new ArrayList<>(Arrays.asList(weightsArray));

        PearsonCorrelationCoefficient instance = new PearsonCorrelationCoefficient();
        float expResult = -1;
        float result = instance.weightedSimilarity(v1, v2, weights);

        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testPerfectCorrelation_weighted() throws Exception {

        Float[] array1 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        Float[] array2 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        Float[] weightsArray = {0.2f, 0.2f, 0.2f, 0.2f, 0.2f};

        List<Float> v1 = new ArrayList<>(Arrays.asList(array1));
        List<Float> v2 = new ArrayList<>(Arrays.asList(array2));
        List<Float> weights = new ArrayList<>(Arrays.asList(weightsArray));

        PearsonCorrelationCoefficient instance = new PearsonCorrelationCoefficient();
        float expResult = 1;
        float result = instance.weightedSimilarity(v1, v2, weights);

        assertEquals(expResult, result, 0.0001);
    }

}
