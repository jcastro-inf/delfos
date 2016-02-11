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
package delfos.experiment.casestudy;

import java.io.PrintStream;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;

/**
 * Listener por defecto que imprime los eventos de cambio en un Stream de
 * salida. Permite limitar la salida para que se haga una vez cada X
 * milisegundos, como muy rápido.
 *
* @author Jorge Castro Gallardo
 * 
 * @version 1.0 14-Mayo-2013
 */
public class ExecutionProgressListener_default implements ExecutionProgressListener {

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
    public ExecutionProgressListener_default(PrintStream out, long verbosePeriod) {
        this.out = out;
        this.verbosePeriod = verbosePeriod;
        chronometer = new Chronometer();
    }

    @Override
    public void executionProgressChanged(String proceso, int percent, long remainingMiliSeconds) {

        boolean begin = percent == 0;
        boolean finish = percent == 100;
        boolean repeated = percent == lastProgressPercent && proceso.equals(lastProgressJob);
        boolean timeTrigger = chronometer.getTotalElapsed() >= verbosePeriod;
        if (((begin || finish) && !repeated)
                || timeTrigger) {

            String message = proceso + " --> "
                    + percent + "% --> "
                    + DateCollapse.collapse(remainingMiliSeconds);
            out.println(message);
            lastProgressJob = proceso;
            lastProgressPercent = percent;

            chronometer.reset();
        }
    }
}
