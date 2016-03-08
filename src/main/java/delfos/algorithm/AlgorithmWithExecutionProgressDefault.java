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

import delfos.common.parameters.ParameterOwnerAdapter;
import java.util.LinkedList;
import java.util.List;

/**
 * Adaptador que implementa la interfaz
 * {@link delfos.algorithm.AlgorithmWithExecutionProgress}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 17-Julio-2013
 */
public abstract class AlgorithmWithExecutionProgressDefault extends ParameterOwnerAdapter implements AlgorithmWithExecutionProgress {

    public AlgorithmWithExecutionProgressDefault() {
    }

    /**
     * List of objects that are notified when the execution progress changes.
     */
    private final List<AlgorithmExecutionProgressListener> listeners = new LinkedList<>();

    /**
     * Creates an event that describes the execution progress of an algorithm.
     *
     * @param task String to describe the task that the algorithm is performing
     * at the moment.
     * @param percent Percent completed of the current task.
     * @param remainingTime Remaining time of the current task.
     */
    protected void fireProgressChanged(String task, int percent, long remainingTime) {
        ExecutionProgressChangedEvent event = new ExecutionProgressChangedEvent(
                this, task, percent, remainingTime);
        listeners.stream().forEach(listener -> listener.progressChanged(event));
    }

    @Override
    public void addProgressListener(AlgorithmExecutionProgressListener listener) {
        listeners.add(listener);
        listener.progressChanged(ExecutionProgressChangedEvent.createEmpty(this));
    }

    @Override
    public void removeProgressListener(AlgorithmExecutionProgressListener listener) {
        listeners.remove(listener);
    }
}
