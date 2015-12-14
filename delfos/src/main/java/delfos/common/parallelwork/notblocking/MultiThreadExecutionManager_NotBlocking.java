package delfos.common.parallelwork.notblocking;

import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.parallelwork.NoMoreSlots;
import delfos.common.parallelwork.Parallelisation;
import delfos.common.parallelwork.Parallelisation.Worker;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.common.parallelwork.Task;
import delfos.common.parallelwork.ThreadOwner;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.common.statisticalfuncions.MeanIterative_Synchronized;
import delfos.experiment.casestudy.ExecutionProgressListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jorge Castro Gallardo
 * @version 29-Mayo-2014
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
public class MultiThreadExecutionManager_NotBlocking<TaskType extends Task> implements Runnable, ThreadOwner {

    private final String taskName;
    private final Class<? extends SingleTaskExecute<TaskType>> singleTaskExecuteClass;
    private final List<Worker> workers = Collections.synchronizedList(new LinkedList<Worker>());

    private final List<TaskType> listOfTasks_Unassigned;
    private final List<TaskType> listOfTasks_Finished;

    private boolean locked = false;
    private boolean running = false;

    private int numTasksAdded = 0;
    private final List<ExecutionProgressListener> listeners;
    private Chronometer chronometerProgressChanged = new Chronometer();

    private final Chronometer chronometerLastTaskFinished = new Chronometer();
    private final MeanIterative meanTimeBetweenTaskFinished = new MeanIterative_Synchronized(100);

    private final Semaphore finalizado = new Semaphore(0);

    /**
     * Random para el mezclado de tareas.
     */
    private final Random random = new Random(System.currentTimeMillis());
    private final String parentThreadName;

    @SuppressWarnings("unchecked") //TODO: eliminar este supressWarning haciendo que el código autocompruebe el cast.
    public MultiThreadExecutionManager_NotBlocking(String taskName,
            Class<? extends SingleTaskExecute<TaskType>> singleTaskExecuteClass) {
        this.listeners = new ArrayList<>();
        this.listOfTasks_Unassigned = Collections.synchronizedList(new ArrayList<>());
        this.listOfTasks_Finished = Collections.synchronizedList(new ArrayList<>());
        this.taskName = taskName;
        this.singleTaskExecuteClass = singleTaskExecuteClass;

        this.parentThreadName = Thread.currentThread().getName();
    }

    public synchronized void runInBackground() {
        if (running) {
            throw new IllegalStateException("Called runInBackground() method twice");
        }

        //TODO: En cuanto entro aquí, ya no me comporto como un worker, sino como un worker requester (libero una hebra)
        Parallelisation.iAmWorkerCreator();

        Thread backgroundThread = new Thread(this, parentThreadName + "->" + taskName + " (background thread)");
        backgroundThread.start();

        running = true;
    }

    @Override
    public void run() {

        synchronized (this) {
            if (!running) {
                throw new IllegalStateException("This method should never invoke, use runInBackground() method instead.");
            }
        }

        Parallelisation.Worker worker;

        int threadInitNumber = 0;

        do {

            try {
                try {
                    worker = new WorkerDefault(parentThreadName + "_" + threadInitNumber++, this);
                    worker.start();
                    workers.add(worker);
                } catch (NoMoreSlots ex) {
                    Parallelisation.waitUntilFreeSlots();
                }
            } catch (InterruptedException ex) {
                Global.showThreadMessageAnnoying("Execution finished signal received.");
                break;
            }

            if (listOfTasks_Unassigned.isEmpty()) {
                synchronized (this) {
                    try {
                        this.wait(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MultiThreadExecutionManager_NotBlocking.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (locked) {
                        if (listOfTasks_Unassigned.isEmpty()) {
                            if (listOfTasks_Finished.size() == numTasksAdded) {
                                break;
                            } else {
                                //Global.showInfoMessage(taskName + " todas las tareas están asignadas, pero no ejecutadas\n");
                            }
                        } else {
                            //Global.showInfoMessage(taskName + " quedan tareas sin asignar.\n");
                        }
                    } else {
                        //Global.showInfoMessage(taskName + " aun no ha sido bloqueado.\n");
                    }
                }
            }
        } while (true);

        Global.showThreadMessageAnnoyingTimestamped(taskName + " finishing, " + listOfTasks_Unassigned.size() + " tasks todo.\n");

        finalizado.release();
    }

    public synchronized void addTask(TaskType task) {
        if (locked) {
            throw new IllegalStateException("Cannot add tasks to a finished executor.");
        }
        if (!running) {
            throw new IllegalStateException("Must call runInBackground() method before adding tasks");
        }

        //Para la primera tarea apunto cuando se añadió al ejecutor.
        if (numTasksAdded == 0) {
            chronometerLastTaskFinished.reset();
        }
        numTasksAdded++;
        listOfTasks_Unassigned.add(task);

        this.notify();
    }

    protected synchronized void taskFinished(TaskType task) {

        listOfTasks_Finished.add(task);

        long totalElapsedFromLastTaskFinished = chronometerLastTaskFinished.getTotalElapsed();
        meanTimeBetweenTaskFinished.addValue(totalElapsedFromLastTaskFinished);

        int remainingTasks = numTasksAdded - listOfTasks_Finished.size();
        long remainingTime = (long) (remainingTasks * meanTimeBetweenTaskFinished.getMean());

        chronometerLastTaskFinished.reset();

        if (listOfTasks_Finished.size() != numTasksAdded) {
            if (chronometerProgressChanged == null) {
                chronometerProgressChanged = new Chronometer();
                fireExecutionProgressChanged(taskName, 0, remainingTime);

            } else {
                if (chronometerProgressChanged.getTotalElapsed() > 2000) {
                    chronometerProgressChanged.reset();
                    int percent = listOfTasks_Finished.size() * 100;
                    percent = percent / numTasksAdded;
                    fireExecutionProgressChanged(taskName, percent, remainingTime);
                }
            }
        }

    }

    public void waitUntilFinished() throws InterruptedException {

        synchronized (this) {
            if (!running) {
                throw new IllegalStateException("Must call runInBackground() method before waiting tasks to finish.");
            }

            Global.showThreadMessageAnnoyingTimestamped("Waiting until " + taskName + " finish.\n");

            locked = true;

            this.notifyAll();
        }

        finalizado.acquire();

        try {
            //TODO: Vuelvo a comportarme como un worker (Vuelvo a reservar una hebra).
            Parallelisation.iAmNotAWorkerCreator();
        } catch (InterruptedException ex) {
            Global.showThreadMessageAnnoying("Execution finished signal received.");
        }

        Global.showThreadMessageAnnoyingTimestamped("Task " + taskName + " finished.\n");

        fireExecutionProgressChanged(taskName, 100, -1);
    }

    class WorkerDefault extends Worker {

        private WorkerDefault(String taskName, MultiThreadExecutionManager_NotBlocking<TaskType> notifyDeath) throws NoMoreSlots, InterruptedException {
            super(notifyDeath, taskName);
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

            if (singleTaskExecute == null) {
                NullPointerException nullPointerException = new NullPointerException("The single task executor could not be created.");
                ERROR_CODES.UNDEFINED_ERROR.exit(nullPointerException);
                throw nullPointerException;
            }

            TaskType taskAssigned = null;
            synchronized (listOfTasks_Unassigned) {
                if (listOfTasks_Unassigned.isEmpty()) {
                    taskAssigned = null;
                } else {
                    try {
                        int index = random.nextInt(listOfTasks_Unassigned.size());
                        taskAssigned = listOfTasks_Unassigned.remove(index);
                    } catch (IndexOutOfBoundsException ex) {
                        taskAssigned = null;
                    }
                }
            }

            while (taskAssigned != null) {
                try {
                    singleTaskExecute.executeSingleTask(taskAssigned);
                } catch (Throwable ex) {
                    Global.showWarning("TaskType failed '" + taskAssigned.toString() + "'.");

                    ERROR_CODES.TASK_EXECUTION_FAILED.exit(ex);
                }
                taskFinished(taskAssigned);

                synchronized (listOfTasks_Unassigned) {
                    if (listOfTasks_Unassigned.isEmpty()) {
                        taskAssigned = null;
                    } else {
                        try {
                            int index = random.nextInt(listOfTasks_Unassigned.size());
                            taskAssigned = listOfTasks_Unassigned.remove(index);
                        } catch (IndexOutOfBoundsException ex) {
                            taskAssigned = null;
                        }
                    }
                }
            }
        }
    }

    public void addExecutionProgressListener(ExecutionProgressListener executionProgressListener) {
        listeners.add(executionProgressListener);
        executionProgressListener.executionProgressChanged("", 0, -1);
    }

    private void fireExecutionProgressChanged(String taskName, int percent, long timeRemaining) {
        listeners.stream().forEach((listener) -> {
            listener.executionProgressChanged(taskName, percent, timeRemaining);
        });
    }

    public Collection<TaskType> getAllFinishedTasks() {
        return listOfTasks_Finished;
    }

    @Override
    public void notifyDeath(Worker workerDead) {
        synchronized (workers) {
            workers.remove(workerDead);
        }
    }
}
