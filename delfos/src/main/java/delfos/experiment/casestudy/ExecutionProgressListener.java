package delfos.experiment.casestudy;

/**
 * Define los métodos que un listener del proceso de ejecución de una ejecución
 * debe implementar para ser notificado de los cambios en el mismo.
 *
* @author Jorge Castro Gallardo
 */
public interface ExecutionProgressListener {

    /**
     *
     * @param proceso Nombre de la tarea que se está ejecutando.
     * @param percent Porcentaje de la tarea actual.
     * @param remainingMiliSeconds Tiempo restante de la tarea actual
     */
    public void executionProgressChanged(String proceso, int percent, long remainingMiliSeconds);
}
