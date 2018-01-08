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
package delfos.results.evaluationmeasures.confusionmatrix;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.RecommendationResults;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa una curva ROC. Proporciona operaciones básicas sobre curvas, como la agregación de varias o el cálculo del
 * área bajo una curva.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class ConfusionMatricesCurve {

    /**
     * Crea una curva de matrices de confusión vacía, es decir, con un único punto que no tiene ningún elemento.
     *
     * @return Curva vacía.
     */
    public static ConfusionMatricesCurve emptyCurve() {
        ConfusionMatricesCurve c = new ConfusionMatricesCurve();
        c.matrices.add(new ConfusionMatrix(0, 0, 0, 0));
        return c;
    }

    public static ConfusionMatricesCurve getConfusionMatricesCurve(RatingsDataset<? extends Rating> testDataset, RecommendationResults recommendationResults, RelevanceCriteria relevanceCriteria) throws RuntimeException {
        int maxLength = 0;
        for (long idUser : testDataset.allUsers()) {
            Collection<Recommendation> lr = recommendationResults.getRecommendationsForUser(idUser);
            if (lr.size() > maxLength) {
                maxLength = lr.size();
            }
        }
        Map<Long, ConfusionMatricesCurve> allUsersCurves = new TreeMap<>();
        AtomicInteger usersWithoutMatrix = new AtomicInteger(0);
        for (long idUser : testDataset.allUsers()) {
            List<Boolean> resultados = new ArrayList<>(recommendationResults.usersWithRecommendations().size());
            Collection<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            try {
                Map<Long, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);
                for (Recommendation r : recommendationList) {
                    long idItem = r.getItem().getId();
                    if (userRatings.containsKey(idItem)) {
                        resultados.add(relevanceCriteria.isRelevant(userRatings.get(idItem).getRatingValue()));
                    } else {
                        resultados.add(false);
                    }
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
            try {
                allUsersCurves.put(idUser, new ConfusionMatricesCurve(resultados));
            } catch (IllegalArgumentException iae) {
                usersWithoutMatrix.incrementAndGet();
            }
        }
        ConfusionMatricesCurve mergedCurves = ConfusionMatricesCurve.mergeCurves(allUsersCurves.values());
        return mergedCurves;
    }
    /**
     * Lista de matrices que almacena las matrices de confusión con cada tamaño de la lista de recomendaciones.
     */
    private List<ConfusionMatrix> matrices = new LinkedList<>();

    /**
     * Crea una curva a partir de las matrices de confusión en cada punto.
     *
     * @param matrices Lista de matrices de confusión. En el índice cero, la matriz de confusión se rellena considerando
     * que no se ha recomendado ningún elemento, es decir, que todos los elementos son falsePositive o trueNegative.
     *
     * @throws IllegalArgumentException Todas las recomendaciones son positivas o negativas, por lo que no se puede
     * calcular la curva.
     */
    public ConfusionMatricesCurve(ConfusionMatrix[] matrices) {
        this.matrices = new ArrayList<>(matrices.length);

        if (matrices.length == 0) {
            throw new IllegalArgumentException("No recomendations provided");
        }

        this.matrices.addAll(Arrays.asList(matrices));
    }

    /**
     * Genera una curva tomando como entrada la lista de si son relevantes o no para el usuario las recomcendaciones.
     *
     * @param listOfRecommendations Lista que representa si la recomendación i del sistema de recomendación es en
     * realidad relevante para el usuario (true) o no (false).
     *
     * <<<<<<< HEAD =======
     * @throw
     * s IllegalArgumentException Todas las recomendaciones son positivas o negativas, por lo que no se puede calcular
     * la curva. >>>>>>> version-evaluation-results
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
            } else if (anterior.getFalsePositiveRate() > confusionMatrix.getFalsePositiveRate()) {
                correcta = false;
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
    public double getAreaUnderROC() {
        double areaUnderROC = 0;

        double tpR_previo = 0;
        double fpR_previo = 0;

        for (int i = 0; i < matrices.size(); i++) {

            double tpr = matrices.get(i).getTruePositiveRate();
            double fpr = matrices.get(i).getFalsePositiveRate();

            //Rectángulo que se forma si no hay incremento en el eje Y (tpr).
            double rectangulo = (fpr - fpR_previo) * (tpR_previo);
            areaUnderROC += rectangulo;

            //Triángulo que se forma si hay incremento.
            double triangulo = ((fpr - fpR_previo) * (tpr - tpR_previo)) / 2;

            areaUnderROC += triangulo;

            tpR_previo = tpr;
            fpR_previo = fpr;
        }
        return (double) areaUnderROC;
    }

    private ConfusionMatrix getMatrixAt(int index) {
        if (index >= size()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size());
        }
        return matrices.get(index);
    }

    /**
     *
     * @param index
     * @return
     */
    public int getFalsePositiveAt(int index) {
        return getMatrixAt(index).getFalsePositive();
    }

    /**
     *
     * @param index
     * @return
     */
    public int getTruePositiveAt(int index) {
        return getMatrixAt(index).getTruePositive();
    }

    /**
     *
     * @param index
     * @return
     */
    public int getFalseNegativeAt(int index) {
        return getMatrixAt(index).getFalseNegative();
    }

    /**
     *
     * @param index
     * @return
     */
    public int getTrueNegativeAt(int index) {
        return getMatrixAt(index).getTrueNegative();
    }

    /**
     *
     * @param index
     * @return
     */
    public double getPrecisionAt(int index) {
        return getMatrixAt(index).getPrecision();
    }

    /**
     *
     * @param index
     * @return
     */
    public double getRecallAt(int index) {
        return getMatrixAt(index).getRecall();
    }

    /**
     * Devuelve el fpr de un punto en una posición dada.
     *
     * @param i Posición.
     * @return Componente fpr del punto.
     */
    public double getFalsePositiveRateAt(int i) {
        return matrices.get(i).getFalsePositiveRate();
    }

    /**
     * Devuelve el tpr de un punto en una posición dada
     *
     * @param i Posición.
     * @return Componente tpr del punto.
     */
    public double getTruePositiveRateAt(int i) {
        return matrices.get(i).getTruePositiveRate();
    }

    /**
     * Comprueba que se compara con una curva y que son exactamente iguales.
     *
     * @param obj Objeto con el que se desea comparar
     * @return True si los objetos son idénticos en contenido, false en otro caso.
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

    /**
     *
     * @return
     */
    public double getAreaPRSpace() {

        double areaUnderPRSPace = 0;

        double precision_previo = 0;
        double recall_previo = 0;

        /**
         * Se ignora el valor en el punto de recomendación 0, ya que la precisión no está definida para dicho punto.
         */
        for (int i = 1; i < matrices.size(); i++) {

            double precision = matrices.get(i).getPrecision();
            double recall = matrices.get(i).getRecall();

            //Rectángulo que se forma si no hay incremento en el eje Y (tpr).
            double rectangulo = (recall - recall_previo) * (precision_previo);
            areaUnderPRSPace += rectangulo;

            //Triángulo que se forma si hay incremento.
            double triangulo = ((recall - recall_previo) * (precision - precision_previo)) / 2;

            areaUnderPRSPace += triangulo;

            precision_previo = precision;
            recall_previo = recall;
        }
        return (double) areaUnderPRSPace;
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

    public String printCurvePrecisionRecallTSV() {
        StringBuilder str = new StringBuilder();
        str.append("listSize").append("\t").append("precision").append("\t").append("recall").append("\n");
        for (int i = 0; i < size(); i++) {
            final double precision = getPrecisionAt(i);
            final double recall = getRecallAt(i);
            str.append(i).append("\t").append(precision).append("\t").append(recall).append("\n");
        }

        return str.toString();
    }

    public String printCurveTSV() {
        StringBuilder str = new StringBuilder();
        str.append("listSize").append("\t")
                .append("tp").append("\t")
                .append("fp").append("\t")
                .append("fn").append("\t")
                .append("tn").append("\n");

        for (int i = 0; i < size(); i++) {
            final double truePositive = getTruePositiveAt(i);
            final double falsePositive = getFalseNegativeAt(i);
            final double falseNegative = getFalseNegativeAt(i);
            final double trueNegative = getTrueNegativeAt(i);

            str.append(i).append("\t")
                    .append(truePositive).append("\t")
                    .append(falsePositive).append("\t")
                    .append(falseNegative).append("\t")
                    .append(trueNegative)
                    .append("\n");
        }

        return str.toString();
    }
}
