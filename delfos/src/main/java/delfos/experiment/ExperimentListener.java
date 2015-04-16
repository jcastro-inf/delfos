package delfos.experiment;

/**
 * Interfaz que define el método que se invoca para notificar del cambio de 
 * estado en la ejecución de un experimento
 * 
* @author Jorge Castro Gallardo
 * @see ExperimentProgress
 * @see AlgorithmExperimentAdapter
 */
public interface ExperimentListener {
    
    /**
     * Este método debe implementar el comportamiento adecuado cuando cambia el
     * estado de ejecución del algoritmo que se observa.
     * @param algorithmExperiment Algoritmo que lanza el evento
     */
    public void progressChanged(ExperimentProgress algorithmExperiment);
}
