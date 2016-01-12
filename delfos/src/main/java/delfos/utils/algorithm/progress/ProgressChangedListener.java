package delfos.utils.algorithm.progress;

/**
 *
 * @author jcastro
 */
public interface ProgressChangedListener {

    public void progressChanged(String task, int percent, long remainingTime);
}
