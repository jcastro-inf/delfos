package delfos.utils.algorithm.progress;

import delfos.common.Chronometer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author jcastro
 */
public final class ProgressChangedController {

    private final String taskName;

    private final int numTasks;

    private final AtomicInteger tasksCompleted = new AtomicInteger(0);

    private final Chronometer chronometer = new Chronometer();

    private final LinkedList<ProgressChangedListener> listeners = new LinkedList<>();

    public ProgressChangedController(String taskName, int numTasks, ProgressChangedListener... listeners) {
        this.taskName = taskName;
        this.numTasks = numTasks;

        if (numTasks < 0) {
            throw new IllegalArgumentException("The number of tasks cannot be negative.");
        }

        for (ProgressChangedListener listener : listeners) {
            addProgressChangedListener(listener);
        }
    }

    /**
     * Notify another task finished and triggers the progress changed event.
     *
     */
    public void setTaskFinished() {
        this.tasksCompleted.incrementAndGet();
        fireProgressChanged();
    }

    public void addProgressChangedListener(ProgressChangedListener listener) {
        if (numTasks == 0) {
            listener.progressChanged(taskName, 100, 0);
        }

        int tasksCompletedNow = this.tasksCompleted.get();

        int percent = (100 * tasksCompletedNow) / numTasks;

        int remainingTasks = numTasks - tasksCompletedNow;

        long remainingTime = -1;
        if (tasksCompletedNow != 0) {
            long timePerTask = chronometer.getTotalElapsed() / tasksCompletedNow;
            remainingTime = remainingTasks * timePerTask;
        }

        listener.progressChanged(taskName, percent, remainingTime);
    }

    private void fireProgressChanged() {
        if (numTasks == 0) {
            listeners.stream().forEach((listener) -> {
                listener.progressChanged(taskName, 100, 0);
            });
        }

        int tasksCompletedNow = this.tasksCompleted.get();

        int percent = (100 * tasksCompletedNow) / numTasks;

        int remainingTasks = numTasks - tasksCompletedNow;
        long timePerTask = chronometer.getTotalElapsed() / tasksCompletedNow;

        long remainingTime = remainingTasks * timePerTask;

        listeners.stream().forEach((listener) -> {
            listener.progressChanged(taskName, percent, remainingTime);
        });
    }

}
