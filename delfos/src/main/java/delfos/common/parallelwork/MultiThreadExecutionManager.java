package delfos.common.parallelwork;

import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.parallelwork.Parallelisation.Worker;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.experiment.casestudy.ExecutionProgressListener;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Paraleliza un proceso de manera que se puede ajustar dinámicamente el número
 * de cpus que se utilizan.
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 22-May-2013
 * @param <TaskType> Clase que tiene la información de entrada y almacena la
 * información de salida de la tarea.
 */
public class MultiThreadExecutionManager<TaskType extends Task> implements Runnable, ThreadOwner {

    private final Collection<TaskType> listOfTasks;
    private int threadInitNumber = 0;

    private final String taskName;
    private final Class<? extends SingleTaskExecute<TaskType>> singleTaskExecuteClass;
    private final List<Worker> workers = Collections.synchronizedList(new LinkedList<Worker>());
    private final List<ExecutionProgressListener> listeners = new LinkedList<>();
    private final MeanIterative timePerTask = new MeanIterative(50);
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

    protected void fireTaskFinished(TaskType t) {
        for (PartialWorkListener<TaskType> listener : partialWorkListener) {
            listener.finishedTask(t);
        }
    }

    @SuppressWarnings("unchecked") //TODO: eliminar este supressWarning haciendo que el código autocompruebe el cast.
    public MultiThreadExecutionManager(String taskName,
            Collection<TaskType> listOfTasks,
            Class<? extends SingleTaskExecute<TaskType>> singleTaskExecuteClass) {
        this.taskName = taskName;
        this.listOfTasks = listOfTasks;
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
        return listOfTasks;
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
    
    public AtomicInteger sizeOfFinished = new AtomicInteger(0);

    @Override
    public void run() {

        this.runningThread = Thread.currentThread();

        Parallelisation.Worker worker;
        timeRunning.reset();

        SingleTaskExecute<TaskType> singleTaskExecute =null;
        try {
            singleTaskExecute = singleTaskExecuteClass.getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        
        if(singleTaskExecute == null){
            throw new IllegalStateException("Cannot create a "+singleTaskExecuteClass.getName());
        }
        SingleTaskExecute<TaskType> finalSingleTaskExecute = singleTaskExecute;
        listOfTasks.parallelStream().map(task -> {
            finalSingleTaskExecute.executeSingleTask(task);
            sizeOfFinished.incrementAndGet();
            return task;
        }).collect(Collectors.toList());
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
        float percent = (sizeOfFinished.get() * 100.0f) / listOfTasks.size();
        return (int) percent;
    }

    private long getRemainingTime() {

        //Si no ha terminado ninguna tarea no puedo predecir el tiempo restante.
        if (sizeOfFinished.get() == 0) {
            return -1;
        }

        long _timePerTask = timeRunning.getTotalElapsed() / sizeOfFinished.get();
        long remainingTime = _timePerTask * (listOfTasks.size() - sizeOfFinished.get());
        return remainingTime;
    }
}
