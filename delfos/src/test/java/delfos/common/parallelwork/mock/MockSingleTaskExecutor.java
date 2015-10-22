package delfos.common.parallelwork.mock;

import delfos.common.Global;
import delfos.common.parallelwork.SingleTaskExecute;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jorge
 */
public class MockSingleTaskExecutor implements SingleTaskExecute<MockTask> {

    @Override
    public void executeSingleTask(MockTask task) {
        if (task.isErroneous) {
            throw new IllegalArgumentException("Error on executing task " + task);
        } else {
            try {
                Thread.sleep(task.delay);
            } catch (InterruptedException ex) {
                Logger.getLogger(MockSingleTaskExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Global.showln("Finished '" + task.toString() + "'");
    }

}
