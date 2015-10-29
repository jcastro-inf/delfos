package delfos.rs;

import java.io.PrintStream;
import java.util.Date;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;

/**
 * Listener por defecto que imprime los eventos de cambio en un Stream de
 * salida. Permite limitar la salida para que se haga una vez cada X
 * milisegundos, como muy rápido.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 23-Jan-2013
 */
public class RecommenderSystemBuildingProgressListener_default implements RecommendationModelBuildingProgressListener {

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
    private boolean beginPrinted;
    private boolean endPrinted;

    /**
     * Constructor por defecto, que establece el stream donde se escribe la
     * información de progreso y se limita el número de escrituras por tiempo.
     *
     * @param out Stream de salida en el que se escriben los mensajes.
     * @param verbosePeriod Tiempo mínimo entre escrituras.
     */
    public RecommenderSystemBuildingProgressListener_default(PrintStream out, long verbosePeriod) {
        this.out = out;
        this.verbosePeriod = verbosePeriod;
        chronometer = new Chronometer();
    }

    @Override
    public synchronized void buildingProgressChanged(String actualJob, int percent, long remainingTime) {

        if (percent == 0) {
            printInfo(actualJob, percent, remainingTime);
            beginPrinted = true;
        } else {
            beginPrinted = false;
            if (percent == 100) {
                printInfo(actualJob, percent, remainingTime);
                endPrinted = true;
            } else {
                endPrinted = false;
            }
        }

        boolean repeated = percent == lastProgressPercent && actualJob.equals(lastProgressJob);
        boolean timeTrigger = chronometer.getTotalElapsed() >= verbosePeriod;
        if (!repeated || timeTrigger) {

            printInfo(actualJob, percent, remainingTime);

        }
    }

    private void printInfo(String actualJob, int percent, long remainingTime) {
        String message = new Date().toString()+": "+actualJob + " --> "
                + percent + "% --> "
                + DateCollapse.collapse(remainingTime);
        out.println(message);
        chronometer.reset();
        lastProgressJob = actualJob;
        lastProgressPercent = percent;

    }
}
