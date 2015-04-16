package delfos.algorithm;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 17-jul-2013
 */
public interface AlgorithmProgressListener {

    /**
     * Método por el que se notifica de cambios en el algoritmo.
     *
     * @param algorithm Algoritmo que ha cambiado.
     */
    public void progressChanged(Algorithm algorithm);
}
