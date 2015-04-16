package delfos.algorithm;

import java.io.PrintStream;
import java.util.Date;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;

/**
 * Listener por defecto, que escribe en la salida indicada el progreso con una
 * frecuencia no superior al tiempo indicado.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 22-May-2013
 */
public class AlgorithmProgressListener_default implements AlgorithmProgressListener {

    /**
     * Cronómetro para controlar el tiempo entre escrituras.
     */
    private Chronometer chronometer;
    /**
     * Stream de salida para escribir los mensajes.
     */
    private final PrintStream out;
    /**
     * Último porcentaje registrado e imprimido en el stream.
     */
    private int lastProgressPercent = -1;
    /**
     * Última tarea registrada e imprimido en el stream.
     */
    private String lastProgressJob = "emptyJob";
    /**
     * Tiempo mínimo que transcurre entre escrituras.
     */
    private long verbosePeriod;

    /**
     * Constructor por defecto, que establece el stream donde se escribe la
     * información de progreso y se limita el número de escrituras por tiempo.
     *
     * @param out Stream de salida en el que se escriben los mensajes.
     * @param verbosePeriod Tiempo mínimo entre escrituras.
     */
    public AlgorithmProgressListener_default(PrintStream out, long verbosePeriod) {
        this.out = out;
        this.verbosePeriod = verbosePeriod;
        chronometer = new Chronometer();
    }

    private void executionProgressChanged(String actualJob, int percent, long remainingTime) {
        boolean begin = percent == 0;
        boolean finish = percent == 100;
        boolean repeated = percent == lastProgressPercent && actualJob.equals(lastProgressJob);
        boolean timeTrigger = chronometer.getTotalElapsed() >= verbosePeriod;
        if (((begin || finish) && !repeated)
                || timeTrigger) {
            printProgress(actualJob, percent, remainingTime);
            lastProgressPercent = percent;

            chronometer.reset();
        }
    }

    @Override
    public void progressChanged(Algorithm algorithm) {

        String task = algorithm.getProgressTask();
        int percent = algorithm.getProgressPercent();
        long remainingTime = algorithm.getProgressRemainingTime();

        executionProgressChanged(task, percent, remainingTime);
    }

    private void printProgress(String actualJob, int percent, long remainingTime) {
        String message = new Date().toString() + " -- " + actualJob + " --> "
                + percent + "% --> "
                + DateCollapse.collapse(remainingTime);
        out.println(message);
        lastProgressJob = actualJob;
    }
}
