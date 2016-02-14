package delfos.common.parallelwork.mock;

import delfos.common.parallelwork.Task;

/**
 *
 * @author Jorge
 */
public class MockTask extends Task {

    long delay;
    boolean isErroneous;

    public MockTask(long delay) {
        this(delay, false);
    }

    public MockTask(long delay, boolean isErroneous) {
        this.delay = delay;
        this.isErroneous = isErroneous;
    }

    @Override
    public String toString() {
        if (isErroneous) {
            return "Erroneous task";

        } else {
            return "Task work for " + delay + "ms";
        }
    }

}
