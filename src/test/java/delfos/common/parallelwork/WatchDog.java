package delfos.common.parallelwork;

import java.util.LinkedList;
import java.util.List;
import delfos.common.Chronometer;

/**
 *
 * @version 21-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class WatchDog implements Runnable {

    private final long time;
    private final Chronometer chronometer = new Chronometer();

    private boolean finishSafely = false;

    List<WatchDogListener> listeners = new LinkedList<>();

    /**
     *
     * @param time Tiempo que el watchdog espera antes de activarse.
     */
    public WatchDog(long time) {
        this.time = time;
    }

    public void addWatchDogListener(WatchDogListener watchDogListener) {
        listeners.add(watchDogListener);
    }

    public void removeWatchDogListener(WatchDogListener watchDogListener) {
        listeners.remove(watchDogListener);
    }

    /**
     * Notifica a todos los listeners de que el tiempo del watchdog ha expirado
     * sin señales de vida.
     */
    public void notifyWatchdogExpired() {
        listeners.stream().forEach((watchDogListener) -> {
            watchDogListener.notifyWatchDogExpired();
        });
    }

    @Override
    public synchronized void run() {

        boolean watchdogExpired = false;
        while (!watchdogExpired && !finishSafely) {
            long timeElapsed = chronometer.getTotalElapsed();
            watchdogExpired = timeElapsed > time;
            if (watchdogExpired) {
                notifyWatchdogExpired();
            } else {
                try {
                    wait(time);
                } catch (InterruptedException ex) {
                    // Ignore exception
                    watchdogExpired = true;
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }

        if (watchdogExpired) {
            notifyWatchdogExpired();
        }
    }

    /**
     * Da señales de vida al watchdog, para que resetee su contador para
     * dispararse.
     */
    public synchronized void signal() {
        chronometer.reset();
    }

    public void finish() {
        finishSafely = true;
    }
}
