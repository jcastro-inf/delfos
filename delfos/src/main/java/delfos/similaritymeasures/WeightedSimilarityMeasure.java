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

import delfos.common.exceptions.CouldNotComputeSimilarity;
import java.util.List;

/**
 * Interfaz que extiende la funcionalidad de las medidas de similitud para que
 * soporten ponderación de las dimensiones de los vectores.
 *
 * <p>
 * <p>
 * La similitud es un valor entre 0 y 1, 0 cuando los vectores son completamente
 * distintos y 1 cuando son completamente iguales.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @see SimilarityMeasure
 * @see BasicSimilarityMeasure
 * @see WeightedSimilarityMeasure
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public interface WeightedSimilarityMeasure extends BasicSimilarityMeasure {

    /**
     * Calcula la medida de similitud entre los vectores v1 y v2 utilizando la
     * ponderación indicada en el parámetro weights.
     *
     * @param v1 Valores del vector 1 a comparar.
     * @param v2 Valores del vector 2 a comparar.
     * @param weights Valores de ponderación de cada dimesión.
     *
     * @return Valor de similitud entre ambos vectores.
     *
     * @throws CouldNotComputeSimilarity
     * @throws IllegalArgumentException Si los vectores no tienen la misma
     * dimensión o la suma de las ponderaciones es mayor que 1.
     */
    public float weightedSimilarity(float[] v1, float[] v2, float[] weights);

    /**
     * Calcula la medida de similitud entre los vectores v1 y v2 utilizando la
     * ponderación indicada en el parámetro weights.
     *
     * @param v1 Valores del vector 1 a comparar.
     * @param v2 Valores del vector 2 a comparar.
     * @param weights Valores de ponderación de cada dimesión.
     *
     * @return Valor de similitud entre ambos vectores.
     *
     * @throws CouldNotComputeSimilarity
     * @throws IllegalArgumentException Si los vectores no tienen la misma
     * dimensión o la suma de las ponderaciones es mayor que 1.
     */
    public float weightedSimilarity(List<Float> v1, List<Float> v2, List<Float> weights);
}
