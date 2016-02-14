package delfos.common.parallelwork;

/**
 *
 * @version 21-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public interface WatchDogListener {

    /**
     * Metodo llamado cuando el watchdog agota el tiempo de espera sin recibir
     * señales.
     */
    public void notifyWatchDogExpired();

}
