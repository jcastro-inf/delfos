package delfos.common.parallelwork.notblocking;

import delfos.common.parallelwork.notblocking.MultiThreadExecutionManager_NotBlocking;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import delfos.common.Global;
import delfos.common.parallelwork.mock.MockSingleTaskExecutor;
import delfos.common.parallelwork.mock.MockTask;
import delfos.constants.DelfosTest;

/**
 *
 * @version 29-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class MultiThreadExecutionManager_NotBlockingTest extends DelfosTest {

    public MultiThreadExecutionManager_NotBlockingTest() {
    }

    @Test
    public void testNoTasks() {
        final String methodName = "testNoTasks";
        Global.showMessage(methodName + "\n");
        MultiThreadExecutionManager_NotBlocking<MockTask> multiThread = new MultiThreadExecutionManager_NotBlocking<>(
                methodName,
                MockSingleTaskExecutor.class);

        multiThread.runInBackground();
        try {
            multiThread.waitUntilFinished();
        } catch (InterruptedException ex) {
            fail("No interruption allowed");
        }
        Global.showMessage(methodName + " finished\n");
    }

    @Test
    public void testLongTask() {
        final String methodName = "testLongTask";
        Global.showMessage(methodName + "\n");
        MultiThreadExecutionManager_NotBlocking<MockTask> multiThread = new MultiThreadExecutionManager_NotBlocking<>(
                methodName,
                MockSingleTaskExecutor.class);
        multiThread.runInBackground();

        multiThread.addTask(new MockTask(300));
        try {
            multiThread.waitUntilFinished();
        } catch (InterruptedException ex) {
            fail("No interruption allowed");
        }
        Global.showMessage(methodName + " Finished\n");
    }

    @Test
    public void testShortTask() {
        final String methodName = "testShortTask";
        Global.showMessage(methodName + "\n");
        MultiThreadExecutionManager_NotBlocking<MockTask> multiThread = new MultiThreadExecutionManager_NotBlocking<>(
                methodName,
                MockSingleTaskExecutor.class);
        multiThread.runInBackground();

        multiThread.addTask(new MockTask(0));
        try {
            multiThread.waitUntilFinished();
        } catch (InterruptedException ex) {
            fail("No interruption allowed");
        }
        Global.showMessage(methodName + " Finished\n");
    }

    @Test
    public void testLongTasks() {
        final String methodName = "testLongTasks";
        Global.showMessage(methodName + "\n");
        MultiThreadExecutionManager_NotBlocking<MockTask> multiThread = new MultiThreadExecutionManager_NotBlocking<>(
                methodName,
                MockSingleTaskExecutor.class);
        multiThread.runInBackground();

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(200));
        multiThread.addTask(new MockTask(250));
        multiThread.addTask(new MockTask(120));
        multiThread.addTask(new MockTask(150));
        multiThread.addTask(new MockTask(070));
        try {
            multiThread.waitUntilFinished();
        } catch (InterruptedException ex) {
            fail("No interruption allowed");
        }
        Global.showMessage(methodName + " Finished\n");
    }

    @Test
    public void testLongTaskWithSleep() {
        final String methodName = "testLongTaskWithSleep";
        Global.showMessage(methodName + "\n");
        MultiThreadExecutionManager_NotBlocking<MockTask> multiThread = new MultiThreadExecutionManager_NotBlocking<>(
                methodName,
                MockSingleTaskExecutor.class);
        multiThread.runInBackground();

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(300));

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(300));

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(300));

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(300));

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(300));

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(300));

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(300));

        multiThread.addTask(new MockTask(300));
        multiThread.addTask(new MockTask(300));

        try {
            Global.showMessage("Sleeping for 500ms\n");
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Global.showError(ex);
        }
        Global.showMessage("main thread runing again\n");
        try {
            multiThread.waitUntilFinished();
        } catch (InterruptedException ex) {
            fail("No interruption allowed");
        }
        assertEquals("The number of finished tasks must be equal to the number of generated tasks", 16, multiThread.getAllFinishedTasks().size());
        Global.showMessage(methodName + " Finished\n");

    }

    @Test
    public void testLongTaskWithSleepBeforeAdd() throws InterruptedException {
        final String methodName = "testLongTaskWithSleepBeforeAdd";
        Global.showMessage(methodName + "\n");
        MultiThreadExecutionManager_NotBlocking<MockTask> multiThread = new MultiThreadExecutionManager_NotBlocking<>(
                methodName,
                MockSingleTaskExecutor.class);
        multiThread.runInBackground();

        Global.showMessage("Sleeping for 5000ms\n");
        Thread.sleep(5000);

        multiThread.addTask(new MockTask(300));

        Global.showMessage("Sleeping for 500ms\n");
        Thread.sleep(500);

        Global.showMessage(methodName + "main thread runing again\n");
        try {
            multiThread.waitUntilFinished();
        } catch (InterruptedException ex) {
            fail("No interruption allowed");
        }

        assertEquals("The number of finished tasks must be equal to the number of generated tasks", 1, multiThread.getAllFinishedTasks().size());

        Global.showMessage(methodName + " Finished\n");
    }

    @Test
    public void testManyTasks() throws InterruptedException {
        final String methodName = "testManyTasks";
        Global.showMessage(methodName + "\n");
        MultiThreadExecutionManager_NotBlocking<MockTask> multiThread = new MultiThreadExecutionManager_NotBlocking<>(
                methodName,
                MockSingleTaskExecutor.class);
        multiThread.runInBackground();

        final int numTasks = 1000;
        Random random = new Random(0);
        for (int i = 0; i < numTasks; i++) {
            int index = random.nextInt(3);
            switch (index) {
                case 0:
                    multiThread.addTask(new MockTask(0));
                    break;
                case 1:
                    multiThread.addTask(new MockTask(50));
                    break;
                case 2:
                    multiThread.addTask(new MockTask(150));
                    break;
                default:
                    throw new IllegalStateException("arg");
            }

        }
        try {
            multiThread.waitUntilFinished();
        } catch (InterruptedException ex) {
            fail("No interruption allowed");
        }

        assertEquals("The number of finished tasks must be equal to the number of generated tasks", numTasks, multiThread.getAllFinishedTasks().size());

        Global.showMessage(methodName + " Finished\n");
    }

}
