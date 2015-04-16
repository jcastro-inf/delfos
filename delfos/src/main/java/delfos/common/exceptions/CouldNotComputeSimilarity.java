package delfos.common.exceptions;

import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.similaritymeasures.WeightedSimilarityMeasure;

/**
 * Excepción que se lanza cuando no se puede calcular la similitud de dos
 * objetos, ya sea porque no se tienen suficientes valores o porque la medida no
 * está definida para esos parámetros
 *
* @author Jorge Castro Gallardo
 *
 * @see BasicSimilarityMeasure
 * @see CollaborativeSimilarityMeasure
 * @see WeightedSimilarityMeasure
 */
public class CouldNotComputeSimilarity extends Exception {

    private static final long serialVersionUID = 1L;
    /**
     * Constructor por defecto de la excepción que asigna el mensaje
     * <code>msg</code>
     *
     * @param msg Mensaje a asignar a la excepción
     */
    public CouldNotComputeSimilarity(String msg) {
        super(msg);
    }

    public CouldNotComputeSimilarity(Throwable cause) {
        super(cause);
    }
}
