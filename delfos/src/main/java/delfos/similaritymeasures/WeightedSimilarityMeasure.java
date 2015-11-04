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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
