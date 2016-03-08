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

import delfos.common.parameters.ParameterOwner;

/**
 * Interface to define the methods that an experiment that is able to notify its
 * progress must implement
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public interface AlgorithmWithExecutionProgress extends ParameterOwner {

    /**
     * Adds an object to be notified when the execution progress changes.
     *
     * @param listener Object to be notified of changes.
     */
    public void addProgressListener(AlgorithmExecutionProgressListener listener);

    /**
     * Removes an object that no longer is notified of changes in the execution
     * progress.
     *
     * @param listener Object that is not notified.
     */
    public void removeProgressListener(AlgorithmExecutionProgressListener listener);
}
