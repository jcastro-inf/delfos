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
