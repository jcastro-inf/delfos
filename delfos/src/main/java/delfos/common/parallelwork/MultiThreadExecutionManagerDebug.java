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
import java.util.Collection;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <TaskType>
 *
 * @deprecated The parallel execution should be done using
 * {@link java.util.function.Function}, by iterating over the list of the
 * objects with the data of the task. Also the objects that perform the
 * execution should be refactored to implement
 * {@link java.util.function.Function} and execute the code over the data
 * object.
 */
public class MultiThreadExecutionManagerDebug<TaskType extends Task> extends MultiThreadExecutionManager<TaskType> {

    public MultiThreadExecutionManagerDebug(String taskName, Collection<TaskType> listOfTasks, Class<? extends SingleTaskExecute<TaskType>> singleTaskExecuteClass) {
        super(taskName, listOfTasks, singleTaskExecuteClass);
    }

    @Override
    public void run() {

        SingleTaskExecute<TaskType> singleTaskExecute;
        try {
            singleTaskExecute = singleTaskExecuteClass.newInstance();
        } catch (InstantiationException ex) {
            ERROR_CODES.UNDEFINED_ERROR.exit(ex);
            throw new IllegalStateException("arg");
        } catch (IllegalAccessException ex) {
            Global.showError(new IllegalAccessError("Cannot create an instance of " + singleTaskExecuteClass + ", check access modifiers for default constuctor."));
            ERROR_CODES.UNDEFINED_ERROR.exit(ex);
            throw new IllegalStateException("arg");
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
