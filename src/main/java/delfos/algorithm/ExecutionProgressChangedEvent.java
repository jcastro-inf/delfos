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
package delfos.algorithm;

/**
 * Event to notify the execution progress of an algorithm.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ExecutionProgressChangedEvent {

    /**
     * Creates an empty event, that only have the algorithm that has triggered
     * it.
     *
     * @param algorithm Algorithm that triggers the event.
     * @return
     */
    public static ExecutionProgressChangedEvent createEmpty(AlgorithmWithExecutionProgressDefault algorithm) {
        return new ExecutionProgressChangedEvent(algorithm, "", 0, -1);
    }

    /**
     * String to describe the task that the algorithm is performing at the
     * moment.
     */
    private final String task;

    /**
     * Percent completed of the current task.
     */
    private final int percent;

    /**
     * Remaining time of the current task.
     */
    private final long remainingTime;

    /**
     * Algorithm that triggers the event.
     */
    private final AlgorithmWithExecutionProgress algorithm;

    /**
     * Creates an event that describes the execution progress of an algorithm.
     *
     * @param algorithm Algorithm that triggers the event.
     * @param task String to describe the task that the algorithm is performing
     * at the moment.
     * @param percent Percent completed of the current task.
     * @param remainingTime Remaining time of the current task.
     */
    public ExecutionProgressChangedEvent(AlgorithmWithExecutionProgress algorithm, String task, int percent, long remainingTime) {
        this.algorithm = algorithm;
        this.task = task;
        this.percent = percent;
        this.remainingTime = remainingTime;
    }

    public int getPercent() {
        return percent;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    /**
     * Returns the task being performed by the algorithm.
     *
     * @return String to describe the task that the algorithm is performing at
     * the moment.
     */
    public String getTask() {
        return task;
    }

    /**
     * Algorithm that has triggered the event.
     *
     * @return Algorithm that has triggered the event.
     */
    public AlgorithmWithExecutionProgress getAlgorithmWithExecutionProgress() {
        return algorithm;
    }

}
