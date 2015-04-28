package delfos.common.parallelwork;

import delfos.ERROR_CODES;
import delfos.common.Global;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Clase para controlar el número de hebras extra que la biblioteca de
 * recomendación utiliza.
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 21-May-2013
 */
public final class Parallelisation {

    private static int limitNumThreads;
    /**
     * Objeto con el que se realiza la exclusión mútua, para sincronizar todas
     * las hebras.
     */
    private static final Object exMut = 0;
    private static final Semaphore threadsAvailable;
    /**
     * Todas las hebras que se han entregado.
     */
    private static final Set<Long> allWorkers = Collections.synchronizedSet(new TreeSet<Long>());
    /**
     * Todas las hebras que piden worker.
     */
    private static final Set<Long> allWorkerCreators = Collections.synchronizedSet(new TreeSet<Long>());

    static {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        limitNumThreads = availableProcessors;
        threadsAvailable = new Semaphore(availableProcessors, true);
    }

    public static void setMaxCPU(int numCPU) {
        synchronized (exMut) {
            int difference = numCPU - limitNumThreads;

            if (difference == 0) {
                //Nada, no hay cambios
            } else {
                if (difference > 0) {
                    //Estan dando mas recursos
                    threadsAvailable.release(difference);
                } else {
                    try {
                        //Están eliminando hebras.
                        threadsAvailable.acquire(-difference);
                    } catch (InterruptedException ex) {
                        ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                    }
                }
            }

            limitNumThreads = numCPU;
            Global.showInfoMessage("The number of available active threads has been set to " + limitNumThreads + "\n");

            if (limitNumThreads > Runtime.getRuntime().availableProcessors()) {
                Global.showInfoMessage("The number of threads is greater than the number of available processors!! ( " + limitNumThreads + " > " + Runtime.getRuntime().availableProcessors() + " )");
            }
        }
    }

    /**
     * Espera hasta que algún hilo queda libre. No asegura que hayan hebras
     * libres una vez se ha finalizado este método, por lo que se debe comprobar
     * si se ha podido adquirir una nueva hebra. En caso contrario se recomienda
     * volver a llamar este método.
     *
     * @throws java.lang.InterruptedException
     */
    public static void waitUntilFreeSlots() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        long threadID = Thread.currentThread().getId();

        Global.showThreadMessageAnnoying(threadName + " -> " + threadID + ": Espero hebras worker libres.");

        threadsAvailable.acquire();
        threadsAvailable.release();

    }

    public static void iAmWorkerCreator() {

        String threadName = Thread.currentThread().getName();
        long threadID = Thread.currentThread().getId();

        Global.showThreadMessageAnnoying(threadName + " thread is now a Worker Creator.");

        synchronized (exMut) {
            if (!allWorkers.contains(threadID)) {
                //Soy una hebra fuera del sistema de paralelismo de esta biblioteca, no se hace nada.
                //throw new IllegalStateException("I am not a worker, illegal method.");
                return;
            }
        }

        //Era un trabajador y ahora soy un creador de trabajadores, libero un recurso.
        threadsAvailable.release();
        allWorkers.remove(threadID);
        allWorkerCreators.add(threadID);

    }

    public static void iAmNotAWorkerCreator() throws InterruptedException {

        String threadName = Thread.currentThread().getName();
        long threadID = Thread.currentThread().getId();

        synchronized (exMut) {
            if (!allWorkerCreators.contains(threadID)) {
                //Soy una hebra fuera del sistema de paralelismo de esta biblioteca.
                //throw new IllegalStateException("I am not a worker creator, illegal method.");
                return;
            }
        }

        //Era un creador de trabajadores y ahora soy un trabajador, acquire un recurso.
        threadsAvailable.acquire();
        allWorkers.add(threadID);
        allWorkerCreators.remove(threadID);

        Global.showThreadMessageAnnoying(threadName + " thread is now a Worker.");

        Global.showThreadMessageAnnoying(threadName + " thread finished being a Worker Creator.");

    }

    public static abstract class Worker extends Thread {

        final ThreadOwner parent;
        final long hebraPadre;

        public Worker(ThreadOwner parent, String taskName) throws NoMoreSlots, InterruptedException {
            super(taskName);

            String threadName = Thread.currentThread().getName();
            long threadID = Thread.currentThread().getId();

            this.parent = parent;
            hebraPadre = Thread.currentThread().getId();

            //Se pueden entregar hebras con normalidad.
            boolean tryAcquire = threadsAvailable.tryAcquire(50, TimeUnit.MILLISECONDS);

            if (tryAcquire == false) {
                throw new NoMoreSlots();
            }

            Global.showThreadMessageAnnoying(">>>> Created thread >>>> " + threadName + " --> " + taskName + " ");

            //Agrego todas las hebras creadas de esta manera, para llevar un control.
            allWorkers.add(this.getId());
        }

        /**
         * Realiza el trabajo asignado a este worker.
         */
        protected abstract void work();

        @Override
        public final void run() {
            try {
                work();
                die();
            } catch (Throwable ex) {
                Global.showError(ex);
                die();
            }
            throw new ThreadDeath();
        }

        protected final void die() {
            long threadID = Thread.currentThread().getId();

            parent.notifyDeath(this);
            if (allWorkers.contains(threadID)) {
                //Hilo de trabajo
                threadsAvailable.release();
                allWorkers.remove(threadID);
            } else {
                if (allWorkerCreators.contains(threadID)) {
                    throw new IllegalArgumentException("A worker creator cannot die!, first notify this thread is not a workerCreator");
                } else {
                    throw new IllegalArgumentException("Unexpected situation, this thread seems to be out of the library parallelisation system.");
                }
            }
            Global.showThreadMessageAnnoying("<<<< Thread died    <<<< " + Thread.currentThread().getName() + Thread.currentThread().getId());
        }
    }

    public static void printSchedulingInfo(PrintStream stream) {

        synchronized (exMut) {

            stream.println("\tMONITORIZATION: Threads available for the library: " + limitNumThreads);
            int availablePermits = threadsAvailable.availablePermits();
            int queueLength = threadsAvailable.getQueueLength();
            if (availablePermits != 0) {
                stream.println("\tMONITORIZATION: There are " + availablePermits + " available threads");
            } else {
                stream.println("\tMONITORIZATION: No available threads, queue length: " + queueLength);
            }
            stream.println("\tMONITORIZATION: allWorkers          --> " + allWorkers.size());
            stream.println("\tMONITORIZATION: allWorkerCreators   --> " + allWorkerCreators.size());
        }
    }
}
