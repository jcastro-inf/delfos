/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.algorithm;

import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import java.io.PrintStream;
import java.util.Date;

/**
 * Listener por defecto, que escribe en la salida indicada el progreso con una
 * frecuencia no superior al tiempo indicado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 22-May-2013
 */
public class AlgorithmExecutionProgressListenerDefault implements AlgorithmExecutionProgressListener {

    /**
     * Cronómetro para controlar el tiempo entre escrituras.
     */
    private final Chronometer chronometer;
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
    private final long verbosePeriod;

    /**
     * Constructor por defecto, que establece el stream donde se escribe la
     * información de progreso y se limita el número de escrituras por tiempo.
     *
     * @param out Stream de salida en el que se escriben los mensajes.
     * @param verbosePeriodInMiliseconds Tiempo mínimo entre escrituras.
     */
    public AlgorithmExecutionProgressListenerDefault(PrintStream out, long verbosePeriodInMiliseconds) {
        this.out = out;
        this.verbosePeriod = verbosePeriodInMiliseconds;
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
    public void progressChanged(ExecutionProgressChangedEvent event) {
        String task = event.getTask();
        int percent = event.getPercent();
        long remainingTime = event.getRemainingTime();

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
