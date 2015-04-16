package delfos.similaritymeasures;

import delfos.common.parameters.ParameterOwner;

/**
 * Interfaz que define los métodos básicos de una medida de similitud. Se han
 * definido las interfaces hijas {@link BasicSimilarityMeasure} y
 * {@link WeightedSimilarityMeasure} para diferenciar el caso en que una medida
 * de similitud no tenga ponderación (en el primer caso) o sí la tenga (en el
 * segundo).
 *
 * @see WeightedSimilarityMeasure
 * @see SimilarityMeasure
 * @see BasicSimilarityMeasure
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 *
 * @version Unknown date
 * @version 1.1 25-Abril-2013 Javadoc completo.
 */
public interface SimilarityMeasure extends ParameterOwner {

    /**
     * Devuelve el nombre de la medida de similitud. Normalmente el código de
     * esta función es el siguiente:
     *
     * <p>
     * <p>
     * public String getName(){
     * <p>
     * return this.getClass().getSimpleName();
     * <p>
     * }
     * <p>
     * <p>
     *
     *
     * @return Nombre de la medida de similitud.
     */
    @Override
    public String getName();

    /**
     * Devuelve el nombre de la medida de similitud. Normalmente el código de
     * esta función es el siguiente:
     *
     * <p>
     * <p>
     * public String toString(){
     * <p>
     * return this.getName();
     * <p>
     * }
     * <p>
     * <p>
     *
     *
     * @return Nombre de la medida de similitud.
     */
    @Override
    public String toString();

    /**
     * Compara las medidas de similitud. Devuelve true si son iguales en
     * funcionamiento. Esto se simplifica comprobando que son objetos de la
     * misma clase y que tienen los mismos parámetros.
     *
     * @param obj
     * @return true si son iguales
     */
    @Override
    public boolean equals(Object obj);

    /**
     * Implementa el codigo hash a partir del nombre de la medida.
     *
     * @return Código hash del nombre de la medida.
     */
    @Override
    public int hashCode();
}
