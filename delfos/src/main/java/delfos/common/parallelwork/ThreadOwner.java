package delfos.common.parallelwork;

import delfos.common.parallelwork.Parallelisation.Worker;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 31-May-2013
 */
public interface ThreadOwner {
    
    public void notifyDeath(Worker workerDead);

}
