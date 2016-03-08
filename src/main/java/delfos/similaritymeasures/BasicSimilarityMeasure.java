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
package delfos.similaritymeasures;

import java.util.List;

/**
 * Interfaz que define los métodos de una medida de similitud básica. Una medida
 * de similitud básica soporta la comparación de dos vectores de valores reales.
 * Para usar medidas con ponderación, ver {@link WeightedSimilarityMeasure}.
 *
 * <p>
 * <p>
 * La similitud es un valor entre 0 y 1, 0 cuando los vectores son completamente
 * distintos y 1 cuando son completamente iguales.
 *
 * @see WeightedSimilarityMeasure
 * @see SimilarityMeasure
 * @see BasicSimilarityMeasure
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public interface BasicSimilarityMeasure extends SimilarityMeasure {

    /**
     * Calcula la medida de similitud entre los vectores v1 y v2.
     *
     * @param v1 Valores del vector 1 a comparar.
     * @param v2 Valores del vector 2 a comparar.
     *
     * @return Valor de similitud entre ambos vectores.
     *
     * @throws IllegalArgumentException Si los vectores no tienen la misma
     * dimensión.
     */
    public double similarity(double[] v1, double[] v2);

    /**
     * Calcula la medida de similitud entre los vectores v1 y v2.
     *
     * @param v1 Valores del vector 1 a comparar.
     * @param v2 Valores del vector 2 a comparar.
     *
     * @return Valor de similitud entre ambos vectores.
     *
     * @throws IllegalArgumentException Si los vectores no tienen la misma
     * dimensión.
     */
    public double similarity(List<Double> v1, List<Double> v2);
}
