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

import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.ExecutionProgressListener;

import java.io.File;
import java.util.LinkedList;

/**
 * Interfaz que establece los métodos mínimos que un objeto que realize una experimentación con algoritmos debe
 * implementar.
 *
 * @version 1.0 (métodos para notificación del progreso de ejecución)
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class ExperimentAdapter extends ParameterOwnerAdapter implements Experiment {

    public static final Parameter RESULTS_DIRECTORY = new Parameter(
            "RESULTS_DIRECTORY",
            new DirectoryParameter(new File("temp")));

    /**
     * Objetos que desean ser notificados de cambios en la ejecución del experimento
     */
    private final LinkedList<ExperimentListener> algorithmExperimentListerners = new LinkedList<>();
    private final LinkedList<ExecutionProgressListener> executionProgressListeners = new LinkedList<>();
    /**
     * Nombre de la tarea actual se está ejecutando en esta ejecución.
     */
    private String executionProgressTask = "Waiting";
    /**
     * Nombre de la tarea que el experimento está ejecutando.
     */
    private String experimentProgressTask = "Waiting";
    /**
     * Porcentaje completado de la ejecución actual.
     */
    private int executionProgressPercent = 0;
    /**
     * Porcentaje completado del experimento.
     */
    private int experimentProgressPercent = 0;
    /**
     * Predicción de tiempo restante de la ejecución en milisegundos.
     */
    private long executionProgressRemainingTime = -1;
    /**
     * Predicción de tiempo restante del experimento en milisegundos.
     */
    private long experimentProgressRemainingTime = -1;

    /**
     * Establece los valores actuales del progreso de la ejecución actual y notifica a los {@link ExperimentListener}
     * registrados.
     *
     * @param task Nombre de la tarea actual de la ejecución.
     * @param percent Porcentaje completado de la ejecución.
     * @param remainingTime Predicción de tiempo restante en milisegundos
     */
    protected void setExecutionProgress(String task, int percent, long remainingTime) {
        executionProgressTask = task;
        executionProgressPercent = percent;

        if (remainingTime != -1) {
            executionProgressRemainingTime = remainingTime;
        }

        fireExecutionProgressChanged();

        int actual = this.getVueltaActual();
        int max = this.getNumVueltas();
        double percentExperiment = (actual * 100.0f + executionProgressPercent) / max;
        int newPercentExperiment = (int) percentExperiment;
        if (newPercentExperiment != experimentProgressPercent) {
            experimentProgressPercent = (int) (percentExperiment);
            fireAlgorithmExperimentProgressChanged();
        }
    }

    /**
     * Establece los valores actuales del progreso del experimento y notifica a los {@link ExperimentListener}
     * registrados.
     *
     * @param task Nombre de la tarea actual.
     * @param percent Porcentaje completado del experimento.
     * @param remainingTime Predicción del tiempo restante del experimetno en milisegundos
     */
    protected void setExperimentProgress(String task, int percent, long remainingTime) {

        if (remainingTime < 0 && remainingTime != -1) {
            throw new IllegalArgumentException("The remaining time must be positive or '-1' (remainingTime was " + remainingTime + ")");
        }

        if (percent < 0) {
            Global.showWarning("Percent must be between 0 and 100 (percent was " + percent + ")\n");
            percent = 0;
        }

        if (percent > 100) {
            Global.showWarning("Percent must be between 0 and 100 (percent was " + percent + ")\n");
            percent = 100;
        }

        experimentProgressTask = task;
        experimentProgressPercent = percent;

        if (remainingTime != -1) {
            experimentProgressRemainingTime = remainingTime;
        }

        fireAlgorithmExperimentProgressChanged();
    }

    @Override
    public int getExperimentProgressPercent() {
        //TODO: Actualizar el progreso del experimento automáticamente, sin tener que setearlo, es decir, calcularlo a partir del progreso de la ejecución y del número de ejecuciones.
        return experimentProgressPercent;
    }

    @Override
    public long getExperimentProgressRemainingTime() {
        return experimentProgressRemainingTime;
    }

    @Override
    public String getExperimentProgressTask() {
        return experimentProgressTask;
    }

    /**
     * Dispara el evento de cambio en el progreso de ejecución del experimento, notificando a todos los listener
     * registrados
     */
    private void fireAlgorithmExperimentProgressChanged() {
        for (ExperimentListener listener : algorithmExperimentListerners) {
            listener.progressChanged(this);
        }
    }

    /**
     * Dispara el evento de cambio en el progreso de ejecución del experimento, notificando a todos los listener
     * registrados
     */
    private void fireExecutionProgressChanged() {
        for (ExecutionProgressListener listener : executionProgressListeners) {
            listener.executionProgressChanged(executionProgressTask, executionProgressPercent, executionProgressRemainingTime);
        }
    }

    @Override
    public void addExperimentListener(ExperimentListener listener) {
        algorithmExperimentListerners.add(listener);
    }

    @Override
    public void removeExperimentListener(ExperimentListener listener) {
        algorithmExperimentListerners.remove(listener);
    }

    public void addExecutionProgressListener(ExecutionProgressListener listener) {
        executionProgressListeners.add(listener);
    }

    public void removeExecutionProgressListener(ExecutionProgressListener listener) {
        executionProgressListeners.remove(listener);
    }

    @Override
    public int getExecutionProgressPercent() {
        return executionProgressPercent;
    }

    @Override
    public long getExecutionProgressRemainingTime() {
        return executionProgressRemainingTime;
    }

    @Override
    public String getExecutionProgressTask() {
        return executionProgressTask;
    }
    /*
     * ====================== Control ejecución actual ======================
     */
    /**
     * Vuelta actual en la que se encuentra el experimento.
     */
    private int vueltaActual;
    /**
     * Número de vueltas que el experimento ejecuta. El número de vueltas es el número de particiones multiplicado por
     * el número de ejecuciones.
     */
    protected int numVueltas;

    @Override
    public int getVueltaActual() {
        return vueltaActual;
    }

    @Override
    public int getNumVueltas() {
        return numVueltas;
    }

    public void setVueltaActual(int vueltaActual) {
        this.vueltaActual = vueltaActual;
    }

    public void setNumVueltas(int numVueltas) {
        this.numVueltas = numVueltas;
    }

    @Override
    public abstract boolean isFinished();

    @Override
    public final long getExperimentRemainingTime() {
        return experimentProgressRemainingTime;
    }

    public void setResultsDirectory(File resultsDirectory) {
        if (resultsDirectory.exists() && !resultsDirectory.isDirectory()) {
            throw new IllegalStateException("Must be a directory");
        }
        setParameterValue(RESULTS_DIRECTORY,resultsDirectory);
    }

    @Override
    public File getResultsDirectory() {
        return (File) getParameterValue(RESULTS_DIRECTORY);
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Experiment){
            Experiment that = ((Experiment) obj);
            return this.equalsIgnoreAliasAndExecutionNumber(that);
        }else{
            return super.equals(obj);
        }
    }

    public boolean equalsIgnoreAliasAndExecutionNumber(Experiment parameterOwner) {
        String regex = ALIAS.getName() + "=([^\\s]+)";
        String regexNumExecutions = CaseStudy.NUM_EXECUTIONS.getName()+ "=([^\\s]+)";

        String myNameWithParameters = this.getNameWithParameters().
                replaceAll(regex, "").
                replaceAll(regexNumExecutions,"");


        String otherNameWithParameters = parameterOwner.getNameWithParameters().
                replaceAll(regex, "").
                replaceAll(regexNumExecutions,"");

        boolean equals = myNameWithParameters.equals(otherNameWithParameters);
        return equals;
    }

}
