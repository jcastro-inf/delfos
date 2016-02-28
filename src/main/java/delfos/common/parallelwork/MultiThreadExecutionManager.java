/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.common.parallelwork;

import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.parallelwork.Parallelisation.Worker;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.experiment.casestudy.ExecutionProgressListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Paraleliza un proceso de manera que se puede ajustar dinámicamente el número
 * de cpus que se utilizan.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 22-May-2013
 * @param <TaskType> Clase que tiene la información de entrada y almacena la
 * información de salida de la tarea.
 *
 * @deprecated The parallel execution should be done using
 * {@link java.util.function.Function}, by iterating over the list of the
 * objects with the data of the task. Also the objects that perform the
 * execution should be refactored to implement
 * {@link java.util.function.Function} and execute the code over the data
 * object.
 */
public class MultiThreadExecutionManager<TaskType extends Task> implements Runnable, ThreadOwner {

    private int threadInitNumber = 0;

    private final String taskName;
    protected final ListOfTasks<TaskType> listOfTasks;
    protected final Class<? extends SingleTaskExecute<TaskType>> singleTaskExecuteClass;
    private final List<Worker> workers = Collections.synchronizedList(new LinkedList<Worker>());
    private final List<ExecutionProgressListener> listeners = new LinkedList<>();
    protected final MeanIterative timePerTask = new MeanIterative(50);
    private final List<PartialWorkListener<TaskType>> partialWorkListener = new LinkedList<>();
    private final Chronometer timeRunning = new Chronometer();
    private final Thread parentThread;
    private Thread runningThread;

    private synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    public void addPartialWorkListener(PartialWorkListener<TaskType> listener) {
        partialWorkListener.add(listener);
    }

    protected void fireTaskFinished() {
        partialWorkListener.stream().forEach((listener) -> listener.finishedTask());
    }

    @SuppressWarnings("unchecked") //TODO: eliminar este supressWarning haciendo que el código autocompruebe el cast.
    public MultiThreadExecutionManager(String taskName,
            Collection<TaskType> listOfTasks,
            Class<? extends SingleTaskExecute<TaskType>> singleTaskExecuteClass) {
        this.taskName = taskName;
        this.listOfTasks = new ListOfTasks<>(listOfTasks);
        this.singleTaskExecuteClass = singleTaskExecuteClass;

        this.parentThread = Thread.currentThread();
    }

    /**
     * El listener es notificado con el progreso completado de esta tarea
     * multihebra. No se calcula tiempo restante estimado.
     *
     * @param executionProgressListener
     */
    public void addExecutionProgressListener(ExecutionProgressListener executionProgressListener) {
        listeners.add(executionProgressListener);
        executionProgressListener.executionProgressChanged(taskName, getPercentCompleted(), -1);
    }

    public void removeExecutionProgressListener(ExecutionProgressListener executionProgressListener) {
        listeners.remove(executionProgressListener);
    }

    protected void fireExecutionProgressChanged() {
        for (ExecutionProgressListener listener : listeners) {
            listener.executionProgressChanged(taskName, getPercentCompleted(), getRemainingTime());
        }
    }

    public Collection<TaskType> getAllFinishedTasks() {
        return listOfTasks.getAllFinishedTasks();
    }

    @Override
    public void notifyDeath(Worker workerDead) {
        synchronized (workers) {
            workers.remove(workerDead);
        }
    }

    boolean error = false;

    private void erroneousTask(TaskType task) {
        error = true;
    }

    class WorkerDefault extends Worker {

        MultiThreadExecutionManager<TaskType> parentMultiThread;

        private WorkerDefault(String taskName, MultiThreadExecutionManager<TaskType> parentMultiThread) throws NoMoreSlots, InterruptedException {
            super(parentMultiThread, taskName);
            this.parentMultiThread = parentMultiThread;
        }

        @Override
        protected void work() {

            SingleTaskExecute<TaskType> singleTaskExecute = null;
            try {
                singleTaskExecute = singleTaskExecuteClass.newInstance();
            } catch (InstantiationException ex) {
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
            } catch (IllegalAccessException ex) {
                Global.showError(new IllegalAccessError("Cannot create an instance of " + singleTaskExecuteClass + ", check access modifiers for default constuctor."));
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
            }

            Collection<TaskType> tasksAssigned = listOfTasks.getNextWork();

            Chronometer c = new Chronometer();
            while (tasksAssigned != null) {
                for (TaskType task : tasksAssigned) {
                    c.reset();
                    try {
                        singleTaskExecute.executeSingleTask(task);
                    } catch (Throwable ex) {

                        Global.showWarning("TaskType failed '" + task.toString() + "'.");
                        Global.showWarning(ex);
                        parentMultiThread.erroneousTask(task);
                        runningThread.interrupt();
                        ERROR_CODES.TASK_EXECUTION_FAILED.exit(ex);
                    }
                    long timeElapsed = c.getTotalElapsed();
                    timePerTask.addValue(timeElapsed);
                    fireTaskFinished();
                }

                listOfTasks.setWorkFinished(tasksAssigned);
                tasksAssigned = listOfTasks.getNextWork();
                fireExecutionProgressChanged();
            }

        }
    }

    @Override
    public void run() {

        this.runningThread = Thread.currentThread();

        Parallelisation.Worker worker;
        timeRunning.reset();

        //TODO: En cuanto entro aquí, ya no me comporto como un worker, sino como un worker requester (libero una hebra)
        Parallelisation.iAmWorkerCreator();

        do {
            try {
                try {
                    worker = new WorkerDefault(taskName + "_" + nextThreadNum(), this);
                    worker.start();
                    workers.add(worker);
                } catch (NoMoreSlots ex) {
                    Parallelisation.waitUntilFreeSlots();
                }
            } catch (InterruptedException ex) {
                //Esta hebra ha sido interrumpida, hay que parar.
                checkInterrupted();
            }

        } while (listOfTasks.sizeOfToDo() != 0);

        try {
            listOfTasks.waitUntilFinished();
        } catch (InterruptedException ex) {
            checkInterrupted();
        }

        try {
            //TODO: Vuelvo a comportarme como un worker (Vuelvo a reservar una hebra).
            Parallelisation.iAmNotAWorkerCreator();
        } catch (InterruptedException ex) {
            checkInterrupted();
        }
    }

    private void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            parentThread.interrupt();
        } else {
            if (error) {
                throw new IllegalStateException("The current thread has been interrupted due to erroneous task");
            }
        }
    }

    private int getPercentCompleted() {
        double percent = (listOfTasks.sizeOfFinished() * 100.0f) / listOfTasks.sizeOfAll();
        return (int) percent;
    }

    private long getRemainingTime() {

        //Si no ha terminado ninguna tarea no puedo predecir el tiempo restante.
        if (listOfTasks.sizeOfFinished() == 0) {
            return -1;
        }

        long _timePerTask = timeRunning.getTotalElapsed() / listOfTasks.sizeOfFinished();
        long remainingTime = _timePerTask * listOfTasks.sizeOfUnfinished();
        return remainingTime;
    }
}
