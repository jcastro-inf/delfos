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
package delfos.utils.algorithm.progress;

import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import java.io.PrintStream;
import java.util.Date;

public class ProgressChangedListenerDefault implements ProgressChangedListener {

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
    private boolean beginPrinted;
    private boolean endPrinted;

    public ProgressChangedListenerDefault(PrintStream out, long verbosePeriod) {
        this.out = out;
        this.verbosePeriod = verbosePeriod;
        chronometer = new Chronometer();
    }

    private void printInfo(String actualJob, int percent, long remainingTime) {
        String message = new Date().toString() + ": " + actualJob + " --> "
                + percent + "% --> "
                + DateCollapse.collapse(remainingTime);
        out.println(message);
        chronometer.reset();
        lastProgressJob = actualJob;
        lastProgressPercent = percent;

    }

    @Override
    public void progressChanged(String actualJob, int percent, long remainingTime) {
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
}
