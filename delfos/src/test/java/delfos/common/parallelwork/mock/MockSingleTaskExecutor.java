package delfos.common.parallelwork.mock;

import java.util.logging.Level;
import java.util.logging.Logger;
import delfos.common.parallelwork.SingleTaskExecute;

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
        System.out.println("Finished '" + task.toString() + "'");
    }

}
