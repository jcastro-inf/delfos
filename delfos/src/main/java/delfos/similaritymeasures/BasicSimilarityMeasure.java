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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
    public float similarity(float[] v1, float[] v2);

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
    public float similarity(List<Float> v1, List<Float> v2);
}
