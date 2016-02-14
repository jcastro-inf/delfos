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
package delfos.experiment;

import java.io.PrintStream;
import java.util.Date;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;

/**
 * Listener por defecto, que escribe en la salida indicada el progreso con una
 * frecuencia no superior al tiempo indicado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 22-May-2013
 */
public class ExperimentListerner_default implements ExperimentListener {

    /**
     * Cronómetro para controlar el tiempo entre escrituras.
     */
    private final Chronometer chronometer;
    /**
     * Stream de salida para escribir los mensajes.
     */
    private final PrintStream out;

    private int executionLastPercent = -1;
    private String executionLastTask = "emptyJob";

    /**
     * Tiempo mínimo que transcurre entre escrituras.
     */
    private final long verbosePeriod;

    /**
     * Constructor por defecto, que establece el stream donde se escribe la
     * información de progreso y se limita el número de escrituras por tiempo.
     *
     * @param out Stream de salida en el que se escriben los mensajes.
     * @param verbosePeriod Tiempo mínimo entre escrituras.
     */
    public ExperimentListerner_default(PrintStream out, long verbosePeriod) {
        this.out = out;
        this.verbosePeriod = verbosePeriod;
        chronometer = new Chronometer();
    }

    @Override
    public void progressChanged(ExperimentProgress algorithmExperiment) {

        String executionTask = algorithmExperiment.getExecutionProgressTask();
        int executionPercent = algorithmExperiment.getExecutionProgressPercent();

        boolean begin = executionPercent == 0;
        boolean finish = executionPercent == 100;
        boolean repeated = executionPercent == executionLastPercent && executionTask.equals(executionLastTask);
        boolean timeTrigger = chronometer.getTotalElapsed() >= verbosePeriod;
        if (((begin || finish) && !repeated)
                || timeTrigger) {
            printProgress(algorithmExperiment);

            executionLastPercent = executionPercent;
            executionLastTask = executionTask;
            chronometer.reset();
        }

    }

    private void printProgress(ExperimentProgress algorithmExperiment) {

        String experimentMessage = new Date().toString() + " ==== Experiment ==== " + algorithmExperiment.getExperimentProgressTask() + " --> "
                + algorithmExperiment.getExperimentProgressPercent() + "% --> "
                + DateCollapse.collapse(algorithmExperiment.getExperimentProgressRemainingTime());

        String executionMessage = new Date().toString() + "      Execution       " + algorithmExperiment.getExecutionProgressTask() + " --> "
                + algorithmExperiment.getExecutionProgressPercent() + "% --> "
                + DateCollapse.collapse(algorithmExperiment.getExecutionProgressRemainingTime());

        out.println(experimentMessage);
        out.println(executionMessage);
    }
}
