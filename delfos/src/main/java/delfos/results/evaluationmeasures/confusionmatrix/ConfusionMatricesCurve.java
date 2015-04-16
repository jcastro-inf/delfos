package delfos.results.evaluationmeasures.confusionmatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Representa una curva ROC. Proporciona operaciones básicas sobre curvas, como
 * la agregación de varias o el cálculo del área bajo una curva.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 (Anterior a verano del 2012)
 * @version 2.0 (11-01-2013)
 * @version 2.01 (14-01-2013) Cambiada la forma en que se construye la curva.
 * @version 2.1 (14-01-2013) Eliminados los métodos deprecated (addPoint) y
 * añadida construcción mediante matrices de confusión y lista de booleanos.
 *
 */
public class ConfusionMatricesCurve {

    /**
     * Crea una curva de matrices de confusión vacía, es decir, con un único
     * punto que no tiene ningún elemento.
     *
     * @return Curva vacía.
     */
    public static ConfusionMatricesCurve emptyCurve() {
        ConfusionMatricesCurve c = new ConfusionMatricesCurve();
        c.matrices.add(new ConfusionMatrix(0, 0, 0, 0));
        return c;
    }
    /**
     * Lista de matrices que almacena las matrices de confusión con cada tamaño
     * de la lista de recomendaciones.
     */
    private List<ConfusionMatrix> matrices = new LinkedList<>();

    /**
     * Crea una curva a partir de las matrices de confusión en cada punto.
     *
     * @param matrices Lista de matrices de confusión. En el índice cero, la
     * matriz de confusión se rellena considerando que no se ha recomendado
     * ningún elemento, es decir, que todos los elementos son falsePositive o
     * trueNegative.
     *
     * @throws IllegalArgumentException Todas las recomendaciones son positivas
     * o negativas, por lo que no se puede calcular la curva.
     */
    public ConfusionMatricesCurve(ConfusionMatrix[] matrices) {
        this.matrices = new ArrayList<>(matrices.length);

        if (matrices.length == 0) {
            throw new IllegalArgumentException("No recomendations provided");
        }

        if (matrices[0].falseNegative == 0) {
            throw new IllegalArgumentException("All recommendations are not relevant.");
        }

        if (matrices[0].trueNegative == 0) {
            throw new IllegalArgumentException("All recommendations are relevant.");
        }

        this.matrices.addAll(Arrays.asList(matrices));
    }

    /**
     * Genera una curva tomando como entrada la lista de si son relevantes o no
     * para el usuario las recomcendaciones.
     *
     * @param listOfRecommendations Lista que representa si la recomendación i
     * del sistema de recomendación es en realidad relevante para el usuario
     * (true) o no (false).
     *
     * @throws IllegalArgumentException Todas las recomendaciones son positivas
     * o negativas, por lo que no se puede calcular la curva.
     */
    public ConfusionMatricesCurve(List<Boolean> listOfRecommendations) {
        int falsePositive = 0;
        int falseNegative = 0;
        int truePositive = 0;
        int trueNegative = 0;

        for (int i = 0; i < listOfRecommendations.size(); i++) {
            if (listOfRecommendations.get(i)) {
                falseNegative++;
            } else {
                trueNegative++;
            }
        }

        if (listOfRecommendations.isEmpty()) {
            throw new IllegalArgumentException("No recomendations provided");
        }

        if (falseNegative == 0) {
            throw new IllegalArgumentException("All test values are not relevant");
        }

        if (trueNegative == 0) {
            throw new IllegalArgumentException("All test values are relevant");
        }

        matrices.add(new ConfusionMatrix(falsePositive, falseNegative, truePositive, trueNegative));

        for (int i = 0; i < listOfRecommendations.size(); i++) {
            if (listOfRecommendations.get(i)) {
                falseNegative--;
                truePositive++;
            } else {
                trueNegative--;
                falsePositive++;
            }

            matrices.add(new ConfusionMatrix(
                    falsePositive, falseNegative, truePositive, trueNegative));
        }
    }

    private ConfusionMatricesCurve() {
    }

    @Override
    public String toString() {
        return "AUROC=" + getAreaUnderROC() + " size=" + size();
    }

    /**
     * Devuelve una cadena que representa la información que la curva contiene.
     *
     * @return Cadena que representa la curva.
     */
    public String printCurve() {

        StringBuilder builder = new StringBuilder();
        for (ConfusionMatrix confusionMatrix : matrices) {
            builder.append(confusionMatrix.getFalsePositiveRate());
            builder.append("\t");
            builder.append(confusionMatrix.getTruePositiveRate());
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Comprueba si la curva es monótona y creciente.
     *
     * @return True si cumple ambas propiedades, false en otro caso.
     */
    public boolean isCorrect() {
        boolean correcta = true;
        ConfusionMatrix anterior = null;

        if (matrices.get(0).getFalsePositiveRate() != 0) {
            correcta = false;
        }
        if (matrices.get(0).getFalsePositiveRate() != 0) {
            correcta = false;
        }
        if (matrices.get(size() - 1).getTruePositiveRate() != 1) {
            correcta = false;
        }
        if (matrices.get(size() - 1).getTruePositiveRate() != 1) {
            correcta = false;
        }

        for (ConfusionMatrix confusionMatrix : matrices) {
            if (anterior == null) {
                anterior = confusionMatrix;
            } else {
                if (anterior.getFalsePositiveRate() > confusionMatrix.getFalsePositiveRate()) {
                    correcta = false;
                }
            }
        }
        return correcta;
    }

    /**
     * Une múltiples curvas en una única curva.
     *
     * @param curvas Curvas a unir.
     * @return Curva que representa la unión de las curvas especificadas.
     */
    public static ConfusionMatricesCurve mergeCurves(Collection<ConfusionMatricesCurve> curvas) {

        int maxSize = 0;
        for (ConfusionMatricesCurve curva : curvas) {
            maxSize = Math.max(maxSize, curva.size());
        }

        ConfusionMatricesCurve aggregatedCurve = new ConfusionMatricesCurve();

        for (int index = 0; index < maxSize; index++) {
            List<ConfusionMatrix> matrices = new LinkedList<>();

            for (ConfusionMatricesCurve curva : curvas) {
                if (index < curva.size()) {
                    matrices.add(curva.getMatrixAt(index));
                } else {
                    matrices.add(curva.getMatrixAt(curva.size() - 1));
                }
            }

            aggregatedCurve.matrices.add(new ConfusionMatrix(matrices));
        }

        return aggregatedCurve;
    }

    /**
     * Devuelve el número de puntos de la curva ROC
     *
     * @return Número de puntos que definen la curva ROC
     */
    public int size() {
        return matrices.size();
    }

    /**
     * Devuelve el área bajo la curva definida.
     *
     * @return Área bajo la curva. El valor siempre está entre 0 y 1.
     */
    public float getAreaUnderROC() {
        double areaUnderROC = 0;

        float tpR_previo = 0;
        float fpR_previo = 0;

        for (int i = 0; i < matrices.size(); i++) {

            float tpr = matrices.get(i).getTruePositiveRate();
            float fpr = matrices.get(i).getFalsePositiveRate();

            //Rectángulo que se forma si no hay incremento en el eje Y (tpr).
            float rectangulo = (fpr - fpR_previo) * (tpR_previo);
            areaUnderROC += rectangulo;

            //Triángulo que se forma si hay incremento.
            float triangulo = ((fpr - fpR_previo) * (tpr - tpR_previo)) / 2;

            areaUnderROC += triangulo;

            tpR_previo = tpr;
            fpR_previo = fpr;
        }
        return (float) areaUnderROC;
    }

    private ConfusionMatrix getMatrixAt(int index) {
        if (index >= size()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size());
        }
        return matrices.get(index);
    }

    public int getFalsePositiveAt(int index) {
        return getMatrixAt(index).getFalsePositive();
    }

    public int getTruePositiveAt(int index) {
        return getMatrixAt(index).getTruePositive();
    }

    public int getFalseNegativeAt(int index) {
        return getMatrixAt(index).getFalseNegative();
    }

    public int getTrueNegativeAt(int index) {
        return getMatrixAt(index).getTrueNegative();
    }

    public float getPrecisionAt(int index) {
        return getMatrixAt(index).getPrecision();
    }

    public float getRecallAt(int index) {
        return getMatrixAt(index).getRecall();
    }

    /**
     * Devuelve el fpr de un punto en una posición dada.
     *
     * @param i Posición.
     * @return Componente fpr del punto.
     */
    public float getFalsePositiveRateAt(int i) {
        return matrices.get(i).getFalsePositiveRate();
    }

    /**
     * Devuelve el tpr de un punto en una posición dada
     *
     * @param i Posición.
     * @return Componente tpr del punto.
     */
    public float getTruePositiveRateAt(int i) {
        return matrices.get(i).getTruePositiveRate();
    }

    /**
     * Comprueba que se compara con una curva y que son exactamente iguales.
     *
     * @param obj Objeto con el que se desea comparar
     * @return True si los objetos son idénticos en contenido, false en otro
     * caso.
     *
     * @see Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfusionMatricesCurve) {
            ConfusionMatricesCurve curve = (ConfusionMatricesCurve) obj;

            if (size() != curve.size()) {
                return false;
            }

            for (int i = 0; i < size(); i++) {
                if (matrices.get(i).falseNegative != curve.matrices.get(i).falseNegative) {
                    return false;
                }
                if (matrices.get(i).falsePositive != curve.matrices.get(i).falsePositive) {
                    return false;
                }
                if (matrices.get(i).trueNegative != curve.matrices.get(i).trueNegative) {
                    return false;
                }
                if (matrices.get(i).truePositive != curve.matrices.get(i).truePositive) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.matrices != null ? this.matrices.hashCode() : 0);
        return hash;
    }

    public float getAreaPRSpace() {

        double areaUnderPRSPace = 0;

        float precision_previo = 0;
        float recall_previo = 0;

        /**
         * Se ignora el valor en el punto de recomendación 0, ya que la
         * precisión no está definida para dicho punto.
         */
        for (int i = 1; i < matrices.size(); i++) {

            float precision = matrices.get(i).getPrecision();
            float recall = matrices.get(i).getRecall();

            //Rectángulo que se forma si no hay incremento en el eje Y (tpr).
            float rectangulo = (recall - recall_previo) * (precision_previo);
            areaUnderPRSPace += rectangulo;

            //Triángulo que se forma si hay incremento.
            float triangulo = ((recall - recall_previo) * (precision - precision_previo)) / 2;

            areaUnderPRSPace += triangulo;

            precision_previo = precision;
            recall_previo = recall;
        }
        return (float) areaUnderPRSPace;
    }
}
