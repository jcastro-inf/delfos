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
 * Interface that algorithm execution progress listeners must implement. It
 * establishes the method which will be called when the progress changes.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public interface AlgorithmExecutionProgressListener {

    /**
     * Method called when the algorithm progress changes.
     *
     * @param event Information of the progress changed event.
     */
    public void progressChanged(ExecutionProgressChangedEvent event);
}
