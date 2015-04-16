package delfos.common.parallelwork;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 04-Jun-2013
 * @param <Task>
 */
public interface PartialWorkListener<Task> {

    @Deprecated
    public void finishedTask(Task t);

    public void finishedTask();
}
