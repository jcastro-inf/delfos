package delfos.algorithm;

import delfos.common.parameters.ParameterOwner;
import delfos.experiment.SeedHolder;

/**
 * Interfaz que define los métodos que un experimento debe implementar para la
 * notificación de su progreso de ejecución.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 17-Julio-2013
 */
public interface Algorithm extends SeedHolder, ParameterOwner {

    /**
     * Obtiene el progreso del algoritmo.
     *
     * @return Porcentaje completado del algoritmo.
     */
    public int getProgressPercent();

    /**
     * Obtiene el tiempo restante del algoritmo .
     *
     * @return Tiempo en milisegundos.
     */
    public long getProgressRemainingTime();

    /**
     * Obtiene la tarea que se está ejecutando en la ejecución .
     *
     * @return Descripción de la tarea.
     */
    public String getProgressTask();

    /**
     * Dispara el evento de cambio en el progreso de ejecución del experimento,
     * notificando a todos los listener registrados
     *
     * @param task Nombre de la tarea que se está realizando.
     * @param percent Porcentaje realizado.
     * @param remainingTime Tiempo restante estimado en milisegundos.
     */
    public void fireProgressChanged(String task, int percent, long remainingTime);

    /**
     * Añade el objeto para que sea notificado de cambios en el progreso del
     * algoritmo.
     *
     * @param listener Objeto que desea ser notificado.
     */
    public void addProgressListener(AlgorithmProgressListener listener);

    /**
     * Elimina el objeto para que no sea notificado de cambios en el progreso
     * del algoritmo.
     *
     * @param listener Objeto que no desea ser notificado.
     */
    public void removeProgressListener(AlgorithmProgressListener listener);
}
