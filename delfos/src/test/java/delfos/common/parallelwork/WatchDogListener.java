package delfos.common.parallelwork;

/**
 *
 * @version 21-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public interface WatchDogListener {

    /**
     * Metodo llamado cuando el watchdog agota el tiempo de espera sin recibir
     * señales.
     */
    public void notifyWatchDogExpired();

}
