package delfos.common.parallelwork;

import delfos.common.parallelwork.MultiThreadExecutionManager;
import java.util.ArrayList;
import java.util.Collection;
import static org.junit.Assert.fail;
import org.junit.Test;
import delfos.common.parallelwork.mock.MockSingleTaskExecutor;
import delfos.common.parallelwork.mock.MockTask;
import delfos.constants.DelfosTest;

/**
 *
 * @author Jorge
 */
public class MultiThreadExecutionManagerTest extends DelfosTest {

    public MultiThreadExecutionManagerTest() {
    }

    public void runSafelyTheeseTasks(Collection<MockTask> listOfTasks) throws InterruptedException {
        MultiThreadExecutionManager<MockTask> multiThreadExecutionManager = new MultiThreadExecutionManager(
                "JUnit test execution",
                listOfTasks,
                MockSingleTaskExecutor.class);

        final Thread currentThread = Thread.currentThread();

        WatchDog watchDog = new WatchDog(20000);
        watchDog.addWatchDogListener(new WatchDogListener() {
            @Override
            public void notifyWatchDogExpired() {
                currentThread.interrupt();
            }
        });
        Thread watchDogThread = new Thread(watchDog, "WatchDog_Thread");
        watchDogThread.start();

        multiThreadExecutionManager.run();

        watchDog.signal();
        watchDog.finish();

        if (currentThread.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    /**
     * Test of run method, of class MultiThreadExecutionManager.
     */
    @Test
    public void testRun() throws InterruptedException {
        System.out.println("run");
        Collection<MockTask> listOfTasks = new ArrayList<>();
        listOfTasks.add(new MockTask(0, false));
        listOfTasks.add(new MockTask(800, false));
        listOfTasks.add(new MockTask(200, false));
        listOfTasks.add(new MockTask(50, false));
        listOfTasks.add(new MockTask(30, false));

        runSafelyTheeseTasks(listOfTasks);

    }

    /**
     * Test of run method, of class MultiThreadExecutionManager.
     */
    @Test
    public void testRunFailed() {
        System.out.println("Run with failures.");
        Collection<MockTask> listOfTasks = new ArrayList<>();
        listOfTasks.add(new MockTask(0, true));
        listOfTasks.add(new MockTask(800, false));
        listOfTasks.add(new MockTask(200, false));
        listOfTasks.add(new MockTask(50, false));
        listOfTasks.add(new MockTask(30, true));

        try {
            runSafelyTheeseTasks(listOfTasks);

            fail("These tasks should never finish executing.");
        } catch (InterruptedException ex) {
            //El watchdog ha avisado, se ha quedado bloqueado
            fail("There has been a deadlock due to failed tasks.");
        } catch (IllegalStateException ex) {
            //Correcto, debe terminar cuando encuentra una tarea errónea
            return;
        }
        fail("This test must end up with an exception IllegalStateException due to erroneous tasks.");
    }

}
