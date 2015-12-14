package delfos.common.parallelwork;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 22-May-2013
 * @param <Task>
 *
 * @deprecated The parallel execution should be done using
 * {@link java.util.function.Function}, by iterating over the list of the
 * objects with the data of the task. Also the objects that perform the
 * execution should be refactored to implement
 * {@link java.util.function.Function} and execute the code over the data
 * object.
 */
public interface SingleTaskExecute<Task> {

    /**
     * Ejecuta la tarea indicada.
     *
     * @param task
     */
    public void executeSingleTask(Task task);
}
