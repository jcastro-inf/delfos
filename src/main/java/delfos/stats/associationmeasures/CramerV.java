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
package delfos.stats.associationmeasures;

import java.util.Iterator;
import java.util.List;
import delfos.common.datastructures.DoubleIndex_MultipleOccurrences;
import delfos.common.Global;

/**
 * Calcula el coeficiente de dependencia Cramer's V o Crammer's Phi de las dos
 * listas de valores indicadas como parámetro. Un coeficiente de dependencia 0
 * significa que no hay relación, 1 significa que las dos variables tienen una
 * dependencia perfecta. El Cramer's V se da en el intervalo [0,1].
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 14-oct-2013
 */
public class CramerV {

    public CramerV() {
    }

    public double association(List<? extends Object> v1, List<? extends Object> v2) throws CannotComputeAssociation {

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
            throw new IllegalArgumentException("The lists are empty, cannot compute pearson correlation coefficient.");
        }

        //composición de las EEDD que ayuda en el cálculo
        DoubleIndex_MultipleOccurrences<Object, Object> doubleIndex = new DoubleIndex_MultipleOccurrences<Object, Object>();

        for (int i = 0; i < v1.size(); i++) {
            doubleIndex.add(v1.get(i), v2.get(i));
        }

        if (doubleIndex.numDistinctType1Values() == 1) {
            throw new CannotComputeAssociation("The variable 1 only has one value.");
        }

        if (doubleIndex.numDistinctType2Values() == 1) {
            throw new CannotComputeAssociation("The variable 2 only has one value.");
        }

        //la variable i va a ser siempre la característica elegido
        //la variable j siempre es la puntuación
        double numerador = 0;
        for (Iterator it = doubleIndex.iteratorType1Values(); it.hasNext();) {
            Object value1 = it.next();
            for (Iterator it2 = doubleIndex.iteratorType2Values(); it2.hasNext();) {
                Object value2 = it2.next();
                double freqPar = doubleIndex.frequencyOfPair(value1, value2);
                double freqValor = doubleIndex.frequencyOfType1Value(value1);
                double freqPuntuacion = doubleIndex.frequencyOfType2Value(value2);

                numerador += Math.pow(freqPar - ((freqValor * freqPuntuacion) / doubleIndex.size()), 2) / ((freqValor * freqPuntuacion) / doubleIndex.size());
            }
        }
        double denominador = doubleIndex.size() * (Math.min(doubleIndex.numDistinctType1Values(), doubleIndex.numDistinctType2Values()) - 1);
        if (numerador == 0 && denominador == 0) {

            Global.showWarning("Numerator and denominator are zero for:");
            Global.showWarning("List variable 1: " + v1);
            Global.showWarning("List variable 2: " + v2);

            throw new CannotComputeAssociation("Numerator and denominator are zero for:\n"
                    + "List variable 1: " + v1 + "\n"
                    + "List variable 2: " + v2 + "\n");

        } else {
            if (denominador == 0) {
                Global.showWarning("Denominator is zero for:");
                Global.showWarning("List variable 1: " + v1);
                Global.showWarning("List variable 2: " + v2);

                throw new CannotComputeAssociation("Denominator is zero for:\n"
                        + "List variable 1: " + v1 + "\n"
                        + "List variable 2: " + v2 + "\n");
            }
            double cramer = (double) Math.sqrt(numerador / denominador);
            return cramer;
        }
    }
}
