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
package delfos.utils.hesitant.similarity;

import java.util.List;
import java.util.ListIterator;

/**
 * Medida de similitud que utiliza el coeficiente de correlación de pearson para
 * comparar la relación de variación de dos vectores. El valor devuelto está en
 * el intervalo [0,1] ya que una relación de variación inversa perfecta
 * (coeficiente de pearson = -1) indicaría que son completamente distintos
 * (similitud = 0)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class PearsonCorrelationCoefficient {

    public float similarity(List<Float> v1, List<Float> v2) {
        double pcc = pearsonCorrelationCoefficient(v1, v2);

        return (float) pcc;
    }

    /**
     * Devuelve el PCC de las listas de valores. El valor está entre -1 y 1. Si
     * las listas están vacías, lanza una excepción
     * {@link CouldNotComputeSimilarity}.
     *
     * @param v1
     * @param v2
     * @return
     */
    public double pearsonCorrelationCoefficient(List<? extends Number> v1, List<? extends Number> v2) {

        validateInputParameters(v1, v2);

        double avg1 = 0;
        double avg2 = 0;

        if (v1.isEmpty() || v2.isEmpty()) {
            return 0;
        }

        ListIterator<? extends Number> i1 = v1.listIterator();
        ListIterator<? extends Number> i2 = v2.listIterator();
        do {

            double value1 = i1.next().doubleValue();
            double value2 = i2.next().doubleValue();

            avg1 += value1 / v1.size();
            avg2 += value2 / v2.size();
        } while (i1.hasNext());

        double numerador = 0;
        double denominador1 = 0;
        double denominador2 = 0;
        i1 = v1.listIterator();
        i2 = v2.listIterator();
        do {
            double value1 = i1.next().doubleValue();
            double value2 = i2.next().doubleValue();

            numerador += (value1 - avg1) * (value2 - avg2);
            denominador1 += (value1 - avg1) * (value1 - avg1);
            denominador2 += (value2 - avg2) * (value2 - avg2);
        } while (i1.hasNext());

        //Cálculo del denominador
        double denominador = Math.sqrt(denominador1 * denominador2);

        double ret = 0;
        if (denominador == 0) {
            return 0;
        } else {
            ret = numerador / denominador;
        }

        return ret;
    }

    private void validateInputParameters(List<? extends Number> v1, List<? extends Number> v2) throws IllegalArgumentException {
        if (v1 == null) {
            throw new IllegalArgumentException("The list v1 cannot be null.");
        }
        if (v2 == null) {
            throw new IllegalArgumentException("The list v2 cannot be null.");
        }
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("The lists have different size: " + v1.size() + " != " + v2.size());
        }
        if (v1.isEmpty()) {
            return;
        }
    }
}
