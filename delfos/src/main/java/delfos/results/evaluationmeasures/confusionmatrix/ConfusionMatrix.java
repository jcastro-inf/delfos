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

import java.util.Collection;
import delfos.results.evaluationmeasures.PRSpace;

/**
 * Matriz de confusión de un algoritmo de clasificación binario.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 (11-01-2013)
 *
 * @see ConfusionMatricesCurve
 * @see PRSpace
 */
public class ConfusionMatrix {

    /**
     * Número de objetos no recomendados que son relevantes, es decir, los
     * elementos que el sistema no recomienda y, teniendo en cuenta la
     * información de valoraciones, se ajustan a las necesidades del usuario
     * (Relevantes no recomendados).
     */
    public final int falseNegative;
    /**
     * Número de objetos recomendados que no son relevantes, es decir, los
     * elementos que el sistema recomienda y, teniendo en cuenta la información
     * de valoraciones, no se ajustan a las necesidades del usuario (No
     * relevantes recomendados).
     */
    public final int falsePositive;
    /**
     * Número de objetos no recomendados que no son relevantes, es decir, los
     * elementos que el sistema no recomienda y, teniendo en cuenta la
     * información de valoraciones, tampoco se ajustan a las necesidades del
     * usuario (No relevantes no recomendados)
     */
    public final int trueNegative;
    /**
     * Número de objetos recomendados que son relevantes, es decir, los
     * elementos que el sistema recomienda y, teniendo en cuenta la información
     * de valoraciones, se ajustan a las necesidades del usuario (Relevantes
     * recomendados).
     */
    public final int truePositive;

    /**
     * Crea el objeto de matriz de confusión.
     *
     * @param falsePositive No relevantes recomendados.
     * @param falseNegative Relevantes no recomendados.
     * @param truePositive Relevantes recomendados.
     * @param trueNegative No relevantes no recomendados.
     */
    public ConfusionMatrix(int falsePositive, int falseNegative, int truePositive, int trueNegative) {
        this.falsePositive = falsePositive;
        this.falseNegative = falseNegative;
        this.truePositive = truePositive;
        this.trueNegative = trueNegative;
    }

    /**
     * Genera una matriz de confusión sumando dos matrices previas.
     *
     * @param matrices Lista de matrices a agregar.
     * @throws IllegalArgumentException Si las matrices no se corresponden con
     * el mismo número de productos recomendados.
     */
    public ConfusionMatrix(Collection<ConfusionMatrix> matrices) {


        int _falseNegative = 0;
        int _falsePositive = 0;
        int _trueNegative = 0;
        int _truePositive = 0;

        {
            int index = 0;
            for (ConfusionMatrix matrix : matrices) {

                _falseNegative += matrix.getFalseNegative();
                _falsePositive += matrix.getFalsePositive();
                _trueNegative += matrix.getTrueNegative();
                _truePositive += matrix.getTruePositive();
                index++;
            }
        }

        this.falseNegative = _falseNegative;
        this.falsePositive = _falsePositive;
        this.trueNegative = _trueNegative;
        this.truePositive = _truePositive;

    }

    /**
     * Obtiene el número de objetos recomendados.
     *
     * @return Productos recomendados
     */
    public int getNumRecommended() {
        return truePositive + falsePositive;
    }

    /**
     * Obtiene el número de objetos no recomendados que son relevantes, es
     * decir, los elementos que el sistema no recomienda y, teniendo en cuenta
     * la información de valoraciones, se ajustan a las necesidades del usuario.
     *
     * <p>Cuenta como un fallo, no recomienda productos que deberían ser
     * recomendados.
     *
     * @return Número de falsos negativos (Relevantes no recomendados).
     */
    public int getFalseNegative() {
        return falseNegative;
    }

    /**
     * Obtiene el número de objetos recomendados que no son relevantes, es
     * decir, los elementos que el sistema recomienda y, teniendo en cuenta la
     * información de valoraciones, no se ajustan a las necesidades del usuario.
     *
     * <p>Cuentan como un fallo, recomienda productos que no procede recomendar.
     *
     * @return Número de falsos positivos (No relevantes recomendados).
     */
    public int getFalsePositive() {
        return falsePositive;
    }

    /**
     * Obtiene el número de objetos no recomendados que no son relevantes, es
     * decir, los elementos que el sistema no recomienda y, teniendo en cuenta
     * la información de valoraciones, tampoco se ajustan a las necesidades del
     * usuario.
     *
     * <p>Cuentan como un acierto, no recomienda productos que efectivamente no
     * procede recomendar.
     *
     * @return Número de verdaderos negativos (No relevantes no recomendados)
     */
    public int getTrueNegative() {
        return trueNegative;
    }

    /**
     * Obtiene el número de objetos recomendados que son relevantes, es decir,
     * los elementos que el sistema recomienda y, teniendo en cuenta la
     * información de valoraciones, se ajustan a las necesidades del usuario.
     *
     * <p>Cuentan como un acierto, recomienda productos que de hecho procede
     * recomendar.
     *
     * @return Número de verdaderos positivos (Relevantes recomendados).
     */
    public int getTruePositive() {
        return truePositive;
    }

    /**
     * Devuelve el falsePositiveRate de la matrix de confusión:
     *
     * <p>fpr = fp / (fp+ tn)
     *
     * @return False positive rate de la matriz de confusión.
     */
    public float getFalsePositiveRate() {
        if ((falsePositive + trueNegative) == 0) {
            return 0;
        } else {
            float falsePositiveRate = falsePositive / ((float) (falsePositive + trueNegative));
            return falsePositiveRate;
        }
    }

    /**
     * Devuelve el truePositiveRate de la matrix de confusión:
     *
     * <p>tpr =tp / (tp+ fn)
     *
     * @return False positive rate de la matriz de confusión.
     */
    public float getTruePositiveRate() {
        if ((truePositive + falseNegative) == 0) {
            return 0;
        } else {
            float truePositiveRate = truePositive / ((float) (truePositive + falseNegative));
            return truePositiveRate;
        }
    }

    /**
     * Devuelve la precisión de la matrix de confusión:
     *
     * <p>precision =tp / (tp+ fp)
     *
     * @return False positive rate de la matriz de confusión.
     */
    public float getPrecision() {
        return truePositive / ((float) truePositive + falsePositive);
    }

    /**
     * Devuelve el recall de la matrix de confusión:
     *
     * <p>recall =tp / (tp+ fn)
     *
     * @return False positive rate de la matriz de confusión.
     */
    public float getRecall() {
        return truePositive / ((float) truePositive + falseNegative);
    }
}
