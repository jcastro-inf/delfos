package delfos.common.parallelwork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Almacena una lista de tareas que se deben ejecutar de forma paralela. Asegura
 * el correcto funcionamiento con múltiples consumidores de tareas.
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
public class ListOfTasks<Task> {

    private int allTasksAdded = 0;

    private final List<Task> tasksTodo;
    private final List<Task> tasksFinished;

    /**
     * Semáforo para esperar a que todas las tareas estén finalizadas.
     */
    private final Semaphore allFinished = new Semaphore(0);

    private boolean finished = true;

    /**
     * Crea una lista de tareas por hacer vacía.
     */
    private ListOfTasks() {
        tasksTodo = Collections.synchronizedList(new LinkedList<Task>());
        tasksFinished = Collections.synchronizedList(new LinkedList<Task>());

    }

    /**
     * Crea una lista de tareas con un conjunto de tareas determinado.
     *
     * @param tasks
     */
    public ListOfTasks(Collection<? extends Task> tasks) {
        this();
        for (Task task : tasks) {
            addWork(task);
        }
    }

    /**
     * Añade el trabajo indicado a la lista de tareas.
     *
     * @param task Trabajo a añadir.
     */
    private void addWork(Task task) {
        tasksTodo.add(task);
        allTasksAdded++;
    }

    /**
     * Devuelve la siguiente pieza de trabajo a realizar.
     *
     * @return Si no quedan tareas a realizar, devuelve null.
     */
    public synchronized Collection<Task> getNextWork() {

        if (tasksTodo.isEmpty()) {
            return null;
        }
        Collection<Task> ret = new ArrayList<>(1);
        ret.add(tasksTodo.remove(0));
        return ret;
    }

    /**
     * Marca una tarea como finalizada.
     *
     * @param task
     */
    public void setWorkFinished(Collection<Task> task) {
        tasksFinished.addAll(task);
        checkAllFinished();
    }

    /**
     * Interrumpe el hilo hasta que terminan de ejecutarse todas las tareas.
     *
     * @throws java.lang.InterruptedException
     */
    public void waitUntilFinished() throws InterruptedException {
        allFinished.acquire();
    }

    /**
     * Comprueba si todas las tareas han terminado de ejecutarse.
     */
    private void checkAllFinished() {
        if (tasksFinished.size() == allTasksAdded) {
            finished = true;
            allFinished.release();
        }
    }

    /**
     * Devuelve el número de tareas, tanto terminadas como en proceso como
     * finalizadas.
     *
     * @return
     */
    public int sizeOfAll() {
        return allTasksAdded;
    }

    /**
     * Devuelve el número de tareas ya finalizadas.
     *
     * @return
     */
    public int sizeOfFinished() {
        return tasksFinished.size();
    }

    /**
     * Devuelve el número de tareas sin terminar.
     *
     * @return
     */
    public int sizeOfUnfinished() {
        return sizeOfAll() - sizeOfFinished();
    }

    public List<Task> getAllFinishedTasks() {
        if (!finished) {
            throw new IllegalStateException("Tried to retrieve the finished task and not all of them are finished.");
        }
        return tasksFinished;
    }

    int sizeOfToDo() {
        return tasksTodo.size();
    }
}
