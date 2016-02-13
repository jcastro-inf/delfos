package delfos.io.xml.confusionmatricescurve;

import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Test;
import delfos.io.xml.UnrecognizedElementException;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatrix;
import delfos.common.Global;
import delfos.constants.DelfosTest;

/**
 * Test para la clases {@link ConfusionMatricesCurve} y
 * {@link ConfusionMatricesCurveXML} Test para la clase de entrada/salida en XML
 * de las curvas definidas a partir de matrices de confusión.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (11-Jan-2013)
 */
public class ConfusionMatricesCurveTest extends DelfosTest {

    public ConfusionMatricesCurveTest() {
    }

    /**
     * Método exclusivo de esta clase de test para generar listas de
     * recomendaciones aleatorias.
     *
     * @param seed Semilla con la que se genera la lista.
     * @param numRecommendations Número de recomendaciones que la lista
     * contiene.
     * @return Lista de booleanos que representa si la recomendación i es en
     * realidad relevante para el usuario o no lo es.
     */
    private List<Boolean> generateDummyRecommendations(long seed, int numRecommendations) {
        Random r = new Random(seed);
        List<Boolean> ret = new ArrayList<Boolean>();

        for (int i = 0; i < numRecommendations; i++) {
            ret.add(r.nextBoolean());
        }

        return ret;
    }

    /**
     * Test para comprobar que el test element y get curve funcionan
     * correctamente si se llaman seguidos. Este test si funciona, quiere decir
     * que la clase funciona correctamente, pero si falla, no describe mucho
     * dónde puede estar el error, ya que testea una gran cantidad de código.
     */
    @Test
    public void testPairOfGetElementAndGetCurve() throws UnrecognizedElementException {

        Global.showInfoMessage("testPairOfGetElementAndGetCurve\n");
        long[] seeds = {045656, 984591, 545640, 455668, 163931};
        int[] sizes = {2, 500, 1000, 2000};

        for (int size : sizes) {
            for (long seed : seeds) {

                try {
                    List<Boolean> r = generateDummyRecommendations(seed, size);
                    ConfusionMatricesCurve curve = new ConfusionMatricesCurve(r);

                    Element element = ConfusionMatricesCurveXML.getElement(curve);

                    ConfusionMatricesCurve newCurve =
                            ConfusionMatricesCurveXML.getConfusionMatricesCurve(element);

                    //Test limits of initial curve.
                    assert curve.getTruePositiveRateAt(0) == 0;
                    assert curve.getFalsePositiveRateAt(0) == 0;
                    assert curve.getTruePositiveRateAt(curve.size() - 1) == 1;
                    assert curve.getFalsePositiveRateAt(curve.size() - 1) == 1;

                    //Test limits of newCurve.
                    assert newCurve.getTruePositiveRateAt(0) == 0;
                    assert newCurve.getFalsePositiveRateAt(0) == 0;
                    assert newCurve.getTruePositiveRateAt(newCurve.size() - 1) == 1;
                    assert newCurve.getFalsePositiveRateAt(newCurve.size() - 1) == 1;

                    //Test area is the same for both
                    assert curve.getAreaUnderROC() == newCurve.getAreaUnderROC();

                    //Test curves are equal
                    assert curve.equals(newCurve);
                } catch (IllegalArgumentException iae) {
                    boolean error = true;
                    if (size == 2 && seed == 19374) {
                        error = false;
                    }
                    if (size == 2 && seed == 984591) {
                        error = false;
                    }
                    if (size == 2 && seed == 545640) {
                        error = false;
                    }
                    if (size == 2 && seed == 163931) {
                        error = false;
                    }

                    if (error) {
                        if (iae.getMessage().startsWith("All recommendations")) {
                            Global.showInfoMessage("if(size == " + size + " && seed == " + seed + "){error = false;}\n");
                        } else {
                            throw iae;
                        }
                    }
                }
            }
        }
    }

    /**
     * Test para comprobar que falla cuando todos los ejemplos son positivos
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAllPositives() {

        Global.showInfoMessage("testAllPositives\n");
        /* El problema se produce porque solo hay ejemplos de una clase(positiva
         o negativa), pero debe haber variedad para que la curva sea correcta*/

        int numExamples = 10;
        List<Boolean> r = new ArrayList<Boolean>(numExamples);
        while (r.size() < numExamples) {
            r.add(Boolean.TRUE);
        }
        ConfusionMatricesCurve curve = new ConfusionMatricesCurve(r);
        assert curve.isCorrect();
    }

    /**
     * Test para comprobar que falla cuando todos los ejemplos son negativos
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAllNegatives() {
        Global.showInfoMessage("testAllNegatives\n");
        int numExamples = 10;
        List<Boolean> r = new ArrayList<Boolean>(numExamples);
        while (r.size() < numExamples) {
            r.add(Boolean.FALSE);
        }
        ConfusionMatricesCurve curve = new ConfusionMatricesCurve(r);
        assert curve.isCorrect();
    }

    /**
     * Comprueba que funciona correctamente en un caso límite (todos los
     * ejemplos son positivos excepto uno).
     */
    @Test
    public void testAllPositivesButOne() {
        Global.showInfoMessage("testAllPositivesButOne\n");
        int numExamples = 10;
        List<Boolean> r = new ArrayList<Boolean>(numExamples);
        while (r.size() < numExamples - 1) {
            r.add(Boolean.TRUE);
        }

        r.add(r.size(), Boolean.FALSE);
        ConfusionMatricesCurve curve = new ConfusionMatricesCurve(r);

        assert curve.isCorrect();
    }

    /**
     * Comprueba que funciona correctamente en un caso límite (todos los
     * ejemplos son negativos excepto uno).
     */
    @Test
    public void testAllNegativesButOne() {
        Global.showInfoMessage("testAllNegativesButOne\n");
        int numExamples = 10;
        List<Boolean> r = new ArrayList<Boolean>(numExamples);
        while (r.size() < numExamples - 1) {
            r.add(Boolean.TRUE);
        }

        r.add(r.size(), Boolean.FALSE);

        ConfusionMatricesCurve curve = new ConfusionMatricesCurve(r);
        assert curve.isCorrect();
    }

    /**
     * Comprobar que el area es 0.5 para listas con valores true y false
     * alternados.
     */
    @Test
    public void testAreaIsZeroPointFive() {
        Global.showInfoMessage("testAreaIsZeroPointFive\n");
        int numElements = 999;

        List<Boolean> recomm_1 = new ArrayList<Boolean>(numElements);
        for (int i = 0; i < numElements; i++) {
            recomm_1.add(i % 2 == 1);
        }
        ConfusionMatricesCurve curve1 = new ConfusionMatricesCurve(recomm_1);
        float areaUnderROC1 = curve1.getAreaUnderROC();
        Global.showInfoMessage("Area = " + areaUnderROC1 + "\n");
        Assert.assertEquals(0.5, areaUnderROC1, 0.001);


        numElements = 1000;
        List<Boolean> recomm_2 = new ArrayList<Boolean>(numElements);
        for (int i = 0; i < numElements; i++) {
            recomm_2.add(i % 2 == 1);
        }
        ConfusionMatricesCurve curve2 = new ConfusionMatricesCurve(recomm_2);
        float areaUnderROC2 = curve2.getAreaUnderROC();
        Global.showInfoMessage("Area = " + areaUnderROC2 + "\n");
        Assert.assertEquals(0.5, areaUnderROC2, 0.001);
    }

    @Test
    public void testAggregationOfCurves() {
        List<Boolean> r1 = generateDummyRecommendations(654321, 10000);
        List<Boolean> r2 = generateDummyRecommendations(123456, 10000);

        //Agregar dos veces lo mismo tiene la misma area
        {
            List<ConfusionMatricesCurve> curves = new ArrayList<ConfusionMatricesCurve>(2);
            curves.add(new ConfusionMatricesCurve(r1));
            curves.add(new ConfusionMatricesCurve(r1));

            ConfusionMatricesCurve agregada = ConfusionMatricesCurve.mergeCurves(curves);

            Assert.assertEquals(new ConfusionMatricesCurve(r1).getAreaUnderROC(), agregada.getAreaUnderROC(), 0.01);
        }


        //Area de la agregada es igual a la media de las áreas para la misma longitud.
        {
            List<ConfusionMatricesCurve> curves = new ArrayList<ConfusionMatricesCurve>(2);
            curves.add(new ConfusionMatricesCurve(r1));
            curves.add(new ConfusionMatricesCurve(r2));

            ConfusionMatricesCurve agregada = ConfusionMatricesCurve.mergeCurves(curves);

            Assert.assertEquals(new ConfusionMatricesCurve(r1).getAreaUnderROC(), agregada.getAreaUnderROC(), 0.01);
        }


        {
            r1 = new ArrayList<Boolean>(3);
            r1.add(true);
            r1.add(false);
            r1.add(true);

            r2 = new ArrayList<Boolean>(2);
            r2.add(true);
            r2.add(false);

            ConfusionMatrix[] matrices = new ConfusionMatrix[4];
            matrices[0] = new ConfusionMatrix(0, 3, 0, 2);
            matrices[1] = new ConfusionMatrix(0, 1, 2, 2);
            matrices[2] = new ConfusionMatrix(2, 1, 2, 0);
            matrices[3] = new ConfusionMatrix(2, 0, 3, 0);


            ConfusionMatricesCurve c1 = new ConfusionMatricesCurve(r1);
            ConfusionMatricesCurve c2 = new ConfusionMatricesCurve(r2);

            List<ConfusionMatricesCurve> listaCurvas = new LinkedList<ConfusionMatricesCurve>();
            listaCurvas.add(c1);
            listaCurvas.add(c2);

            ConfusionMatricesCurve hardCodedAggregation = new ConfusionMatricesCurve(matrices);
            ConfusionMatricesCurve implementedAggregation = ConfusionMatricesCurve.mergeCurves(listaCurvas);


            Global.showInfoMessage(hardCodedAggregation.printCurve() + "\n");

            Assert.assertEquals(
                    hardCodedAggregation.getAreaUnderROC(),
                    implementedAggregation.getAreaUnderROC(),
                    0.0001);
            Assert.assertEquals(
                    2.0 / 3,
                    implementedAggregation.getAreaUnderROC(),
                    0.0001);
            Assert.assertEquals(
                    2.0 / 3,
                    hardCodedAggregation.getAreaUnderROC(),
                    0.0001);
            Assert.assertEquals(
                    0.5,
                    c1.getAreaUnderROC(),
                    0.0001);
            Assert.assertEquals(
                    1,
                    c2.getAreaUnderROC(),
                    0.0001);

            assert hardCodedAggregation.equals(implementedAggregation);
        }
    }
}
