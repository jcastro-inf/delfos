package delfos.common.parallelwork;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 22-May-2013
 */
public interface SingleTaskExecute<Task> {

    /**
     * Ejecuta la tarea indicada.
     *
     * @param task
     */
    public void executeSingleTask(Task task);
}
