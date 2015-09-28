/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.common.parallelwork;

import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.Global;
import java.util.Collection;

@Deprecated
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
                fireTaskFinished(task);
            }

            listOfTasks.setWorkFinished(tasksAssigned);
            tasksAssigned = listOfTasks.getNextWork();
            fireExecutionProgressChanged();
        }

    }
}
